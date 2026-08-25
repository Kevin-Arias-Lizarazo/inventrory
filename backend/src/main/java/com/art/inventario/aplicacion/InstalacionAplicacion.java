package com.art.inventario.aplicacion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.art.inventario.aplicacion.dto.RespuestaInstalacion;
import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.configuracion.SecretoRecuperacion;
import com.art.inventario.dominio.Accion;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.dominio.Usuario;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.BackupCasoDeUso;
import com.art.inventario.puerto.entrada.InstalacionCasoDeUso;
import com.art.inventario.puerto.salida.NivelAccesoPersistencia;
import com.art.inventario.puerto.salida.RegistroAuditoria;
import com.art.inventario.puerto.salida.UsuarioPersistencia;

@Service
public class InstalacionAplicacion implements InstalacionCasoDeUso {

	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withZone(ZoneId.systemDefault());

	private static final String CARACTERES_CLAVE = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*";
	private static final SecureRandom ALEATORIO = new SecureRandom();

	private final UsuarioPersistencia usuarios;
	private final NivelAccesoPersistencia niveles;
	private final BackupCasoDeUso backup;
	private final PasswordEncoder passwordEncoder;
	private final SecretoRecuperacion secreto;
	private final RegistroAuditoria auditoria;
	private final String uploadsDir;

	public InstalacionAplicacion(UsuarioPersistencia usuarios, NivelAccesoPersistencia niveles,
			BackupCasoDeUso backup, PasswordEncoder passwordEncoder, SecretoRecuperacion secreto,
			RegistroAuditoria auditoria, @Value("${app.uploads.dir:uploads}") String uploadsDir) {
		this.usuarios = usuarios;
		this.niveles = niveles;
		this.backup = backup;
		this.passwordEncoder = passwordEncoder;
		this.secreto = secreto;
		this.auditoria = auditoria;
		this.uploadsDir = uploadsDir;
	}

	@Override
	public boolean pendiente() {
		return niveles.usuarioRaizId().isEmpty();
	}

	@Override
	public RespuestaInstalacion completar(String rootPassword, String adminPassword, byte[] dbArchivo, byte[] uploadsZip) {
		if (!pendiente()) {
			throw new DatosInvalidosExcepcion("La instalación ya fue completada");
		}
		if (rootPassword == null || rootPassword.length() < 8) {
			throw new DatosInvalidosExcepcion("La contraseña de root debe tener al menos 8 caracteres");
		}
		if (dbArchivo != null && dbArchivo.length > 0) {
			backup.restaurar(dbArchivo);
		}
		if (uploadsZip != null && uploadsZip.length > 0) {
			extraerZip(uploadsZip, uploadsDir);
		}
		Usuario root = asegurarRoot(rootPassword);
		String claveAdminGenerada = asegurarAdmin(adminPassword);
		String secretoGenerado = secreto.obtener();
		if (secretoGenerado == null || secretoGenerado.isBlank()) {
			secretoGenerado = secreto.generar();
		}
		auditoria.registrar(evento("root", Accion.INSTALACION, "OK",
				"Instalación completada. Root: " + root.getUsername()));
		RespuestaInstalacion respuesta = new RespuestaInstalacion(aRespuesta(root), secretoGenerado);
		// La clave temporal del admin se devuelve UNA vez (solo si fue generada) para
		// que el operador la guarde. Nunca se registra en logs.
		respuesta.setAdminPasswordTemporal(claveAdminGenerada);
		return respuesta;
	}

	private Usuario asegurarRoot(String rootPassword) {
		Optional<Usuario> opt = usuarios.porUsername("root");
		Usuario root;
		if (opt.isPresent()) {
			root = opt.get();
			root.setPasswordHash(passwordEncoder.encode(rootPassword));
			root.setActivo(true);
			root.setNivelAcceso("ROOT");
		} else {
			root = new Usuario();
			root.setUsername("root");
			root.setNombre("Root");
			root.setPasswordHash(passwordEncoder.encode(rootPassword));
			root.setActivo(true);
			root.setNivelAcceso("ROOT");
			root.setFechaCreacion(FORMATO.format(Instant.now()));
		}
		Usuario guardado = usuarios.guardar(root);
		niveles.porCodigo("ROOT").ifPresent(nivel -> {
			nivel.setUsuarioRaizId(guardado.getId());
			niveles.guardar(nivel);
		});
		return guardado;
	}

	private String asegurarAdmin(String adminPassword) {
		boolean existeAdmin = usuarios.todos().stream()
				.anyMatch(u -> "ADMIN".equalsIgnoreCase(u.getNivelAcceso()));
		if (existeAdmin) {
			return null;
		}
		// Si el operador indicó una contraseña se usa; si no, se genera una aleatoria
		// y segura que se devuelve una única vez en la respuesta (nunca en logs).
		String clave = adminPassword;
		if (clave == null || clave.isBlank()) {
			clave = generarClaveTemporal();
		}
		if (clave.length() < 8) {
			throw new DatosInvalidosExcepcion("La contraseña del admin debe tener al menos 8 caracteres");
		}
		Usuario admin = new Usuario();
		admin.setUsername("admin");
		admin.setNombre("Administrador");
		admin.setPasswordHash(passwordEncoder.encode(clave));
		admin.setActivo(true);
		admin.setNivelAcceso("ADMIN");
		admin.setFechaCreacion(FORMATO.format(Instant.now()));
		usuarios.guardar(admin);
		return (adminPassword == null || adminPassword.isBlank()) ? clave : null;
	}

	private static String generarClaveTemporal() {
		StringBuilder sb = new StringBuilder(14);
		for (int i = 0; i < 14; i++) {
			sb.append(CARACTERES_CLAVE.charAt(ALEATORIO.nextInt(CARACTERES_CLAVE.length())));
		}
		return sb.toString();
	}

	private void extraerZip(byte[] zip, String destino) {
		Path dir = Paths.get(destino).toAbsolutePath().normalize();
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			Files.createDirectories(dir);
			ZipEntry entrada;
			while ((entrada = zis.getNextEntry()) != null) {
				if (entrada.isDirectory()) {
					continue;
				}
				Path salida = dir.resolve(entrada.getName()).normalize();
				if (!salida.startsWith(dir)) {
					throw new DatosInvalidosExcepcion("El archivo comprimido contiene rutas no válidas");
				}
				Files.createDirectories(salida.getParent());
				Files.copy(zis, salida, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new DatosInvalidosExcepcion("No se pudo extraer el archivo de uploads: " + e.getMessage());
		}
	}

	private EventoLog evento(String usuario, Accion accion, String resultado, String detalle) {
		EventoLog e = new EventoLog();
		e.setFecha(Instant.now().toString());
		e.setUsuario(usuario);
		e.setAccion(accion);
		e.setResultado(resultado);
		e.setDetalle(detalle);
		return e;
	}

	private static UsuarioRespuesta aRespuesta(Usuario u) {
		UsuarioRespuesta r = new UsuarioRespuesta();
		r.setId(u.getId());
		r.setUsername(u.getUsername());
		r.setNombre(u.getNombre());
		r.setNivel(u.getNivelAcceso());
		r.setActivo(u.getActivo());
		r.setFechaCreacion(u.getFechaCreacion());
		r.setUltimoAcceso(u.getUltimoAcceso());
		r.setFechaBloqueo(u.getFechaBloqueo());
		r.setMotivoBloqueo(u.getMotivoBloqueo());
		return r;
	}
}