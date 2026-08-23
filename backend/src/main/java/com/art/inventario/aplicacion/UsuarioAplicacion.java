package com.art.inventario.aplicacion;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.configuracion.UsuarioAutenticado;
import com.art.inventario.dominio.Accion;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.dominio.Usuario;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.puerto.entrada.UsuarioCasoDeUso;
import com.art.inventario.puerto.salida.NivelAccesoPersistencia;
import com.art.inventario.puerto.salida.RegistroAuditoria;
import com.art.inventario.puerto.salida.SesionPersistencia;
import com.art.inventario.puerto.salida.UsuarioPersistencia;

@Service
public class UsuarioAplicacion implements UsuarioCasoDeUso {

	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withZone(ZoneId.systemDefault());

	public static final Set<String> PERMISOS_ESPECIALES = Set.of();

	private final UsuarioPersistencia usuarios;
	private final NivelAccesoPersistencia niveles;
	private final SesionPersistencia sesiones;
	private final PasswordEncoder passwordEncoder;
	private final RegistroAuditoria auditoria;

	public UsuarioAplicacion(UsuarioPersistencia usuarios, NivelAccesoPersistencia niveles,
			SesionPersistencia sesiones, PasswordEncoder passwordEncoder, RegistroAuditoria auditoria) {
		this.usuarios = usuarios;
		this.niveles = niveles;
		this.sesiones = sesiones;
		this.passwordEncoder = passwordEncoder;
		this.auditoria = auditoria;
	}

	@Override
	public List<UsuarioRespuesta> listar() {
		Long raiz = niveles.usuarioRaizId().orElse(null);
		return usuarios.todos().stream()
				.filter(u -> raiz == null || !raiz.equals(u.getId()))
				.map(this::aRespuesta)
				.toList();
	}

	@Override
	@Transactional
	public UsuarioRespuesta crear(String username, String nombre, String contrasena, String nivel) {
		String usuario = username == null ? "" : username.trim().toLowerCase();
		if (usuario.isBlank() || contrasena == null || contrasena.length() < 8) {
			throw new DatosInvalidosExcepcion(
					"El usuario es obligatorio y la contraseña debe tener al menos 8 caracteres");
		}
		if (!nivelValido(nivel)) {
			throw new DatosInvalidosExcepcion(
					"Solo se pueden crear usuarios con nivel SUPERVISOR, USUARIO o LECTOR");
		}
		if (usuarios.porUsername(usuario).isPresent()) {
			throw new ConflictoExcepcion("Ya existe un usuario con ese nombre");
		}
		Usuario u = new Usuario();
		u.setUsername(usuario);
		u.setNombre(nombre);
		u.setPasswordHash(passwordEncoder.encode(contrasena));
		u.setNivelAcceso(nivel);
		u.setActivo(true);
		u.setFechaCreacion(FORMATO.format(Instant.now()));
		Usuario creado = usuarios.guardar(u);
		log(actual().getUsername(), "ADMIN", Accion.CREAR_USUARIO, "OK",
				"Usuario " + usuario + " con nivel " + nivel);
		return aRespuesta(creado);
	}

	@Override
	@Transactional
	public UsuarioRespuesta cambiarNivel(Long id, String nivel) {
		if (!nivelValido(nivel)) {
			throw new DatosInvalidosExcepcion("Solo se puede asignar el nivel SUPERVISOR, USUARIO o LECTOR");
		}
		Usuario u = obtenerGestionable(id);
		u.setNivelAcceso(nivel);
		Usuario guardado = usuarios.guardar(u);
		invalidarSesiones(u);
		log(actual().getUsername(), "ADMIN", Accion.CAMBIAR_ROL, "OK",
				"Nivel de " + u.getUsername() + " ahora es " + nivel);
		return aRespuesta(guardado);
	}

	@Override
	@Transactional
	public UsuarioRespuesta bloquear(Long id) {
		Usuario u = obtenerGestionable(id);
		u.setActivo(false);
		u.setFechaBloqueo(FORMATO.format(Instant.now()));
		u.setMotivoBloqueo("Bloqueado por el administrador");
		Usuario guardado = usuarios.guardar(u);
		invalidarSesiones(u);
		log(actual().getUsername(), "ADMIN", Accion.BLOQUEO_USUARIO, "OK", "Usuario " + u.getUsername() + " bloqueado");
		return aRespuesta(guardado);
	}

	@Override
	@Transactional
	public UsuarioRespuesta desbloquear(Long id) {
		Usuario u = obtenerGestionable(id);
		u.setActivo(true);
		u.setFechaBloqueo(null);
		u.setMotivoBloqueo(null);
		Usuario guardado = usuarios.guardar(u);
		log(actual().getUsername(), "ADMIN", Accion.DESBLOQUEO_USUARIO, "OK",
				"Usuario " + u.getUsername() + " desbloqueado");
		return aRespuesta(guardado);
	}

	private Usuario obtenerGestionable(Long id) {
		Usuario u = usuarios.porId(id)
				.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
		Long raiz = niveles.usuarioRaizId().orElse(null);
		if (raiz != null && raiz.equals(u.getId())) {
			throw new ConflictoExcepcion("La cuenta root no se puede modificar");
		}
		UsuarioAutenticado actor = actual();
		if (actor != null && u.getId().equals(actor.getId())) {
			throw new ConflictoExcepcion("No puede modificar su propia cuenta desde administración");
		}
		return u;
	}

	private boolean nivelValido(String nivel) {
		return "SUPERVISOR".equalsIgnoreCase(nivel)
				|| "USUARIO".equalsIgnoreCase(nivel)
				|| "LECTOR".equalsIgnoreCase(nivel);
	}

	private void invalidarSesiones(Usuario u) {
		sesiones.bloquearPorUsuario(u.getId());
	}

	private UsuarioAutenticado actual() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado ua) {
			return ua;
		}
		throw new NoEncontradoExcepcion("Sesión inválida");
	}

	private void log(String usuario, String nivel, Accion accion, String resultado, String detalle) {
		EventoLog e = new EventoLog();
		e.setFecha(Instant.now().toString());
		e.setUsuario(usuario);
		e.setRol(nivel);
		e.setAccion(accion);
		e.setResultado(resultado);
		e.setDetalle(detalle);
		auditoria.registrar(e);
	}

	private UsuarioRespuesta aRespuesta(Usuario u) {
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