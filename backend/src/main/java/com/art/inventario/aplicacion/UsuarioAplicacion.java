package com.art.inventario.aplicacion;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.configuracion.UsuarioAutenticado;
import com.art.inventario.dominio.Accion;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.dominio.Rol;
import com.art.inventario.dominio.Usuario;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.puerto.entrada.UsuarioCasoDeUso;
import com.art.inventario.puerto.salida.RegistroAuditoria;
import com.art.inventario.puerto.salida.SesionPersistencia;
import com.art.inventario.puerto.salida.UsuarioPersistencia;

@Service
public class UsuarioAplicacion implements UsuarioCasoDeUso {

	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withZone(ZoneId.systemDefault());

	private final UsuarioPersistencia usuarios;
	private final SesionPersistencia sesiones;
	private final PasswordEncoder passwordEncoder;
	private final RegistroAuditoria auditoria;

	public UsuarioAplicacion(UsuarioPersistencia usuarios, SesionPersistencia sesiones,
			PasswordEncoder passwordEncoder, RegistroAuditoria auditoria) {
		this.usuarios = usuarios;
		this.sesiones = sesiones;
		this.passwordEncoder = passwordEncoder;
		this.auditoria = auditoria;
	}

	@Override
	public List<UsuarioRespuesta> listar() {
		return usuarios.todos().stream()
				.filter(u -> !u.esRoot())
				.map(this::aRespuesta)
				.toList();
	}

	@Override
	@Transactional
	public UsuarioRespuesta crear(String username, String nombre, String contrasena, Rol rol) {
		String usuario = username == null ? "" : username.trim().toLowerCase();
		if (usuario.isBlank() || contrasena == null || contrasena.length() < 8) {
			throw new DatosInvalidosExcepcion(
					"El usuario es obligatorio y la contraseña debe tener al menos 8 caracteres");
		}
		if (rol == null || rol == Rol.ADMIN) {
			throw new DatosInvalidosExcepcion("Solo se pueden crear usuarios con rol USUARIO o LECTOR");
		}
		if (usuarios.porUsername(usuario).isPresent()) {
			throw new ConflictoExcepcion("Ya existe un usuario con ese nombre");
		}
		Usuario u = new Usuario();
		u.setUsername(usuario);
		u.setNombre(nombre);
		u.setPasswordHash(passwordEncoder.encode(contrasena));
		u.setRol(rol);
		u.setActivo(true);
		u.setEsRoot(false);
		u.setFechaCreacion(FORMATO.format(Instant.now()));
		Usuario creado = usuarios.guardar(u);
		log(actual().getUsername(), "ADMIN", Accion.CREAR_USUARIO, "OK", "Usuario " + usuario + " con rol " + rol);
		return aRespuesta(creado);
	}

	@Override
	@Transactional
	public UsuarioRespuesta cambiarRol(Long id, Rol rol) {
		if (rol == null || rol == Rol.ADMIN) {
			throw new DatosInvalidosExcepcion("No se puede asignar el rol ADMIN");
		}
		Usuario u = obtenerGestionable(id);
		u.setRol(rol);
		Usuario guardado = usuarios.guardar(u);
		invalidarSesiones(u);
		log(actual().getUsername(), "ADMIN", Accion.CAMBIAR_ROL, "OK",
				"Rol de " + u.getUsername() + " ahora es " + rol);
		return aRespuesta(guardado);
	}

	@Override
	@Transactional
	public UsuarioRespuesta bloquear(Long id) {
		Usuario u = obtenerGestionable(id);
		u.setActivo(false);
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
		Usuario guardado = usuarios.guardar(u);
		log(actual().getUsername(), "ADMIN", Accion.DESBLOQUEO_USUARIO, "OK",
				"Usuario " + u.getUsername() + " desbloqueado");
		return aRespuesta(guardado);
	}

	@Override
	@Transactional
	public UsuarioRespuesta reestablecerContrasena(Long id, String nuevaContrasena) {
		if (nuevaContrasena == null || nuevaContrasena.length() < 8) {
			throw new DatosInvalidosExcepcion("La nueva contraseña debe tener al menos 8 caracteres");
		}
		Usuario u = obtenerGestionable(id);
		u.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
		Usuario guardado = usuarios.guardar(u);
		invalidarSesiones(u);
		log(actual().getUsername(), "ADMIN", Accion.REESTABLECER_CLAVE, "OK",
				"Contraseña restablecida para " + u.getUsername());
		return aRespuesta(guardado);
	}

	private Usuario obtenerGestionable(Long id) {
		Usuario u = usuarios.porId(id)
				.orElseThrow(() -> new NoEncontradoExcepcion("Usuario no encontrado"));
		if (u.esRoot()) {
			throw new ConflictoExcepcion("La cuenta root no se puede modificar");
		}
		UsuarioAutenticado actor = actual();
		if (actor != null && u.getId().equals(actor.getId())) {
			throw new ConflictoExcepcion("No puede modificar su propia cuenta desde administración");
		}
		return u;
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

	private void log(String usuario, String rol, Accion accion, String resultado, String detalle) {
		EventoLog e = new EventoLog();
		e.setFecha(Instant.now().toString());
		e.setUsuario(usuario);
		e.setRol(rol);
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
		r.setRol(u.getRol());
		r.setActivo(u.getActivo());
		r.setEsRoot(u.getEsRoot());
		r.setFechaCreacion(u.getFechaCreacion());
		r.setUltimoAcceso(u.getUltimoAcceso());
		return r;
	}
}