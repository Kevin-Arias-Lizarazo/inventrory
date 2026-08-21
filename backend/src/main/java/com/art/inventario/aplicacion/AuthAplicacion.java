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
import com.art.inventario.puerto.salida.RegistroAuditoria;
import com.art.inventario.puerto.salida.SesionPersistencia;
import com.art.inventario.puerto.salida.UsuarioPersistencia;

@Service
public class AuthAplicacion implements AuthCasoDeUso {

	private static final Duration DURACION_SESION = Duration.ofHours(12);
	private static final Duration DURACION_ACCESS = Duration.ofMinutes(10);
	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withZone(ZoneId.systemDefault());

	private final UsuarioPersistencia usuarios;
	private final SesionPersistencia sesiones;
	private final PasswordEncoder passwordEncoder;
	private final RegistroAuditoria auditoria;
	private final ControlLogin controlLogin;
	private final SecretoRecuperacion secreto;

	public AuthAplicacion(UsuarioPersistencia usuarios, SesionPersistencia sesiones,
			PasswordEncoder passwordEncoder, RegistroAuditoria auditoria, ControlLogin controlLogin,
			SecretoRecuperacion secreto) {
		this.usuarios = usuarios;
		this.sesiones = sesiones;
		this.passwordEncoder = passwordEncoder;
		this.auditoria = auditoria;
		this.controlLogin = controlLogin;
		this.secreto = secreto;
	}

	@Override
	@Transactional
	public RespuestaLogin login(String username, String password, String ip) {
		String nombre = username == null ? "" : username.trim();
		if (nombre.isBlank() || password == null || password.isBlank()) {
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		if (!controlLogin.permitido(nombre)) {
			throw new DatosInvalidosExcepcion("Demasiados intentos fallidos. Espere unos minutos");
		}
		Optional<Usuario> opt = usuarios.porUsername(nombre.toLowerCase());
		if (opt.isEmpty()) {
			controlLogin.registrarFallo(nombre);
			log(nombre, null, Accion.LOGIN_FALLIDO, "FALLIDO", ip, "Usuario inexistente");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		Usuario u = opt.get();
		if (!Boolean.TRUE.equals(u.getActivo())) {
			controlLogin.registrarFallo(nombre);
			log(nombre, u.getRol().name(), Accion.LOGIN_FALLIDO, "FALLIDO", ip, "Cuenta inactiva o bloqueada");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}
		if (!passwordEncoder.matches(password, u.getPasswordHash())) {
			controlLogin.registrarFallo(nombre);
			log(nombre, u.getRol().name(), Accion.LOGIN_FALLIDO, "FALLIDO", ip, "Contraseña incorrecta");
			throw new DatosInvalidosExcepcion("Credenciales inválidas");
		}

		controlLogin.limpiar(nombre);
		u.setUltimoAcceso(FORMATO.format(Instant.now()));
		usuarios.guardar(u);

		String access = SeguridadUtil.generarToken(32);
		String refresh = SeguridadUtil.generarToken(32);
		Instant ahora = Instant.now();
		sesiones.crear(u.getId(), u.getUsername(), u.getRol(), u.getRol().name(),
				SeguridadUtil.hash(access), SeguridadUtil.hash(refresh), ahora,
				ahora.plus(DURACION_SESION), ahora.plus(DURACION_ACCESS));
		log(u.getUsername(), u.getRol().name(), Accion.LOGIN_OK, "OK", ip, "Inicio de sesión");
		RespuestaLogin respuesta = new RespuestaLogin(access, aRespuesta(u));
		respuesta.setRefreshToken(refresh);
		return respuesta;
	}

	@Override
	public void logout(String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			sesiones.porRefreshHash(SeguridadUtil.hash(refreshToken)).ifPresent(s -> {
				log(s.getUsername(), s.getRol().name(), Accion.FIN_SESION, "OK", null, "Cierre de sesión");
				sesiones.terminar(s.getId());
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
		Usuario u = usuarios.porUsername(username)
				.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
		if (!passwordEncoder.matches(contrasenaActual, u.getPasswordHash())) {
			throw new DatosInvalidosExcepcion("La contraseña actual no es correcta");
		}
		u.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
		usuarios.guardar(u);
		controlLogin.registrarCambioClave(username);
		log(username, u.getRol().name(), Accion.CAMBIO_CLAVE, "OK", null, "Cambio de contraseña propia");
	}

	@Override
	@Transactional
	public void recuperarAdmin(String secretoRoot, String nuevaContrasenaAdmin) {
		if (!secreto.valida(secretoRoot)) {
			log("root", "ADMIN", Accion.RECUPERAR_ADMIN, "FALLIDO", null, "Secreto raíz inválido");
			throw new DatosInvalidosExcepcion("Secreto raíz inválido");
		}
		if (nuevaContrasenaAdmin == null || nuevaContrasenaAdmin.length() < 8) {
			throw new DatosInvalidosExcepcion("La nueva contraseña debe tener al menos 8 caracteres");
		}
		Usuario admin = usuarios.todos().stream()
				.filter(u -> !u.esRoot() && u.getRol() == com.art.inventario.dominio.Rol.ADMIN)
				.findFirst()
				.orElseThrow(() -> new NoEncontradoExcepcion("No existe una cuenta de administrador"));
		admin.setPasswordHash(passwordEncoder.encode(nuevaContrasenaAdmin));
		usuarios.guardar(admin);
		controlLogin.registrarCambioClave(admin.getUsername());
		sesiones.bloquearPorUsuario(admin.getId());
		log("root", "ADMIN", Accion.RECUPERAR_ADMIN, "OK", null,
				"Credenciales restablecidas para admin " + admin.getUsername());
	}

	private UsuarioAutenticado actual() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado ua) {
			return ua;
		}
		return null;
	}

	private void log(String usuario, String rol, Accion accion, String resultado, String ip, String detalle) {
		EventoLog e = new EventoLog();
		e.setFecha(Instant.now().toString());
		e.setUsuario(usuario);
		e.setRol(rol);
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
		r.setRol(u.getRol());
		r.setActivo(u.getActivo());
		r.setEsRoot(u.getEsRoot());
		r.setFechaCreacion(u.getFechaCreacion());
		r.setUltimoAcceso(u.getUltimoAcceso());
		return r;
	}
}