package com.art.inventario.aplicacion;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.RespuestaLogin;
import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.configuracion.ControlLogin;
import com.art.inventario.configuracion.SecretoRecuperacion;
import com.art.inventario.configuracion.SeguridadUtil;
import com.art.inventario.configuracion.UsuarioAutenticado;
import com.art.inventario.dominio.Accion;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.dominio.Sesion;
import com.art.inventario.dominio.Usuario;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.puerto.entrada.AuthCasoDeUso;
import com.art.inventario.puerto.salida.NivelAccesoPersistencia;
import com.art.inventario.puerto.salida.RegistroAuditoria;
import com.art.inventario.puerto.salida.SesionPersistencia;
import com.art.inventario.puerto.salida.UsuarioPersistencia;

@Service
public class AuthAplicacion implements AuthCasoDeUso {

	private static final Duration DURACION_SESION = Duration.ofHours(12);
	private static final Duration DURACION_ACCESS = Duration.ofMinutes(10);
	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withZone(ZoneId.systemDefault());
	private static final String CLAVE_RECUPERACION = "__recuperar_admin__";

	private final UsuarioPersistencia usuarios;
	private final NivelAccesoPersistencia niveles;
	private final SesionPersistencia sesiones;
	private final PasswordEncoder passwordEncoder;
	private final RegistroAuditoria auditoria;
	private final ControlLogin controlLogin;
	private final SecretoRecuperacion secreto;

	public AuthAplicacion(UsuarioPersistencia usuarios, NivelAccesoPersistencia niveles,
			SesionPersistencia sesiones, PasswordEncoder passwordEncoder, RegistroAuditoria auditoria,
			ControlLogin controlLogin, SecretoRecuperacion secreto) {
		this.usuarios = usuarios;
		this.niveles = niveles;
		this.sesiones = sesiones;
		this.passwordEncoder = passwordEncoder;
		this.auditoria = auditoria;
		this.controlLogin = controlLogin;
		this.secreto = secreto;
	}

	@Override
	@Transactional
	public RespuestaLogin login(String username, String password, String ip) {
		String nombre = username == null ? "" : username.trim().toLowerCase();
		if (nombre.isBlank() || password == null || password.isBlank()) {
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		if (!controlLogin.permitido(nombre)) {
			throw new DatosInvalidosExcepcion("Demasiados intentos fallidos. Espere unos minutos");
		}
		Optional<Usuario> opt = usuarios.porUsername(nombre);
		if (opt.isEmpty()) {
			controlLogin.registrarFallo(nombre);
			log(nombre, null, Accion.LOGIN_FALLIDO, "FALLIDO", ip, "Usuario inexistente");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		Usuario u = opt.get();
		if (esRaiz(u.getId())) {
			controlLogin.registrarFallo(nombre);
			log(nombre, u.getNivelAcceso(), Accion.LOGIN_FALLIDO, "FALLIDO", ip, "La cuenta raíz no inicia sesión");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		if (!Boolean.TRUE.equals(u.getActivo())) {
			controlLogin.registrarFallo(nombre);
			log(nombre, u.getNivelAcceso(), Accion.LOGIN_FALLIDO, "FALLIDO", ip, "Cuenta inactiva o bloqueada");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		if (!passwordEncoder.matches(password, u.getPasswordHash())) {
			controlLogin.registrarFallo(nombre);
			log(nombre, u.getNivelAcceso(), Accion.LOGIN_FALLIDO, "FALLIDO", ip, "Contraseña incorrecta");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}

		controlLogin.limpiar(nombre);
		u.setUltimoAcceso(FORMATO.format(Instant.now()));
		usuarios.guardar(u);

		String access = SeguridadUtil.generarToken(32);
		String refresh = SeguridadUtil.generarToken(32);
		Instant ahora = Instant.now();
		sesiones.crear(u.getId(), u.getUsername(), u.getNivelAcceso(), u.getNivelAcceso(),
				SeguridadUtil.hash(access), SeguridadUtil.hash(refresh), ahora,
				ahora.plus(DURACION_SESION), ahora.plus(DURACION_ACCESS));
		log(u.getUsername(), u.getNivelAcceso(), Accion.LOGIN_OK, "OK", ip, "Inicio de sesión");
		RespuestaLogin respuesta = new RespuestaLogin(access, aRespuesta(u));
		respuesta.setRefreshToken(refresh);
		return respuesta;
	}

	@Override
	public void logout(String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			sesiones.porRefreshHash(SeguridadUtil.hash(refreshToken)).ifPresent(s -> {
				UsuarioAutenticado ua = actual();
				if (ua == null || ua.getId().equals(s.getUsuarioId())) {
					log(s.getUsername(), s.getNivelAcceso(), Accion.FIN_SESION, "OK", null, "Cierre de sesión");
					sesiones.terminar(s.getId());
				}
			});
		}
		log(actual() == null ? null : actual().getUsername(), null, Accion.LOGOUT, "OK", null, "Cierre de sesión");
	}

	@Override
	public RespuestaLogin renovar(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new DatosInvalidosExcepcion("Sesión inválida");
		}
		Sesion s = sesiones.porRefreshHash(SeguridadUtil.hash(refreshToken))
				.orElseThrow(() -> new DatosInvalidosExcepcion("Sesión inválida"));
		Instant ahora = Instant.now();
		if (s.bloqueada()) {
			throw new DatosInvalidosExcepcion("Sesión inválida");
		}
		try {
			if (Instant.parse(s.getFechaFin()).isBefore(ahora)) {
				throw new DatosInvalidosExcepcion("Sesión expirada");
			}
		} catch (DatosInvalidosExcepcion e) {
			throw e;
		} catch (Exception e) {
			throw new DatosInvalidosExcepcion("Sesión inválida");
		}
		Optional<Usuario> opt = usuarios.porUsername(s.getUsername());
		if (opt.isEmpty() || !Boolean.TRUE.equals(opt.get().getActivo())) {
			throw new DatosInvalidosExcepcion("Sesión inválida");
		}
		String nuevoAccess = SeguridadUtil.generarToken(32);
		sesiones.renovarAccess(s.getId(), SeguridadUtil.hash(nuevoAccess), ahora.plus(DURACION_ACCESS));
		RespuestaLogin respuesta = new RespuestaLogin(nuevoAccess, aRespuesta(opt.get()));
		respuesta.setRefreshToken(refreshToken);
		return respuesta;
	}

	@Override
	public UsuarioRespuesta usuarioActual() {
		UsuarioAutenticado ua = actual();
		if (ua == null) {
			throw new NoEncontradoExcepcion("Sesión inválida");
		}
		Usuario u = usuarios.porUsername(ua.getUsername())
				.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
		return aRespuesta(u);
	}

	@Override
	@Transactional
	public void cambiarContrasena(String username, String contrasenaActual, String nuevaContrasena) {
		if (nuevaContrasena == null || nuevaContrasena.length() < 8) {
			throw new DatosInvalidosExcepcion("La nueva contraseña debe tener al menos 8 caracteres");
		}
		UsuarioAutenticado ua = actual();
		String nombre = ua != null ? ua.getUsername() : (username == null ? "" : username);
		Usuario u = usuarios.porUsername(nombre)
				.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
		if (esRaiz(u.getId())) {
			throw new DatosInvalidosExcepcion("La cuenta raíz no puede cambiar su contraseña por aquí");
		}
		if (!controlLogin.permitido(nombre)) {
			throw new DatosInvalidosExcepcion("Demasiados intentos fallidos. Espere unos minutos");
		}
		if (!passwordEncoder.matches(contrasenaActual, u.getPasswordHash())) {
			controlLogin.registrarFallo(nombre);
			throw new DatosInvalidosExcepcion("La contraseña actual no es correcta");
		}
		controlLogin.limpiar(nombre);
		u.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
		usuarios.guardar(u);
		controlLogin.registrarCambioClave(nombre);
		log(nombre, u.getNivelAcceso(), Accion.CAMBIO_CLAVE, "OK", null, "Cambio de contraseña propia");
	}

	@Override
	@Transactional
	public void cambiarContrasenaTercero(Long objetivoId, String nuevaContrasena, String secretoRoot) {
		if (nuevaContrasena == null || nuevaContrasena.length() < 8) {
			throw new DatosInvalidosExcepcion("La nueva contraseña debe tener al menos 8 caracteres");
		}
		boolean esRaizActor = false;
		if (secretoRoot != null && !secretoRoot.isBlank()) {
			if (!controlLogin.permitido(CLAVE_RECUPERACION)) {
				throw new DatosInvalidosExcepcion("Demasiados intentos. Espere unos minutos");
			}
			if (!secreto.valida(secretoRoot)) {
				controlLogin.registrarFallo(CLAVE_RECUPERACION);
				log("root", "ROOT", Accion.RECUPERAR_ADMIN, "FALLIDO", null, "Secreto raíz inválido");
				throw new DatosInvalidosExcepcion("Secreto raíz inválido");
			}
			controlLogin.limpiar(CLAVE_RECUPERACION);
			esRaizActor = true;
		}

		Usuario objetivo;
		if (esRaizActor) {
			if (objetivoId != null) {
				objetivo = usuarios.porId(objetivoId)
						.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
			} else {
				objetivo = usuarios.todos().stream()
						.filter(u -> "ADMIN".equalsIgnoreCase(u.getNivelAcceso()))
						.findFirst()
						.orElseThrow(() -> new NoEncontradoExcepcion("No existe una cuenta de administrador"));
			}
			if (esRaiz(objetivo.getId())) {
				throw new DatosInvalidosExcepcion("No es posible cambiar la contraseña de root");
			}
			if (!"ADMIN".equalsIgnoreCase(objetivo.getNivelAcceso())) {
				throw new DatosInvalidosExcepcion("ROOT solo puede restablecer la contraseña del admin");
			}
		} else {
			UsuarioAutenticado ua = actual();
			if (ua == null || !"ADMIN".equalsIgnoreCase(ua.getNivel())) {
				throw new DatosInvalidosExcepcion("Acción permitida solo para administrador o root");
			}
			if (objetivoId == null) {
				throw new DatosInvalidosExcepcion("Debe indicar el usuario de destino");
			}
			objetivo = usuarios.porId(objetivoId)
					.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
			if (esRaiz(objetivo.getId())) {
				throw new DatosInvalidosExcepcion("No es posible cambiar la contraseña de root");
			}
			if (objetivo.getId().equals(ua.getId())) {
				throw new DatosInvalidosExcepcion("Use Mi cuenta para cambiar su propia contraseña");
			}
			// Política: un ADMIN solo puede restablecer claves de USUARIO o LECTOR.
			// Se rechaza también ADMIN/SUPERVISOR para evitar escalada de privilegios.
			String nivelObjetivo = objetivo.getNivelAcceso();
			if (!"USUARIO".equalsIgnoreCase(nivelObjetivo) && !"LECTOR".equalsIgnoreCase(nivelObjetivo)) {
				throw new DatosInvalidosExcepcion(
						"Solo es posible cambiar la contraseña de usuarios USUARIO o LECTOR");
			}
		}

		objetivo.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
		usuarios.guardar(objetivo);
		controlLogin.registrarCambioClave(objetivo.getUsername());
		sesiones.bloquearPorUsuario(objetivo.getId());
		log(esRaizActor ? "root" : actual().getUsername(),
				esRaizActor ? "ROOT" : actual().getNivel(), Accion.REESTABLECER_CLAVE, "OK", null,
				"Contraseña restablecida para " + objetivo.getUsername());
	}

	private boolean esRaiz(Long usuarioId) {
		return niveles.usuarioRaizId().map(id -> id.equals(usuarioId)).orElse(false);
	}

	private UsuarioAutenticado actual() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado ua) {
			return ua;
		}
		return null;
	}

	private void log(String usuario, String nivel, Accion accion, String resultado, String ip, String detalle) {
		EventoLog e = new EventoLog();
		e.setFecha(Instant.now().toString());
		e.setUsuario(usuario);
		e.setRol(nivel);
		e.setIp(ip);
		e.setAccion(accion);
		e.setResultado(resultado);
		e.setDetalle(detalle);
		auditoria.registrar(e);
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