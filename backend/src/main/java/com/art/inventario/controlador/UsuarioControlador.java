package com.art.inventario.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.dominio.Rol;
import com.art.inventario.puerto.entrada.UsuarioCasoDeUso;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

	private final UsuarioCasoDeUso servicio;

	public UsuarioControlador(UsuarioCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<UsuarioRespuesta>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@PostMapping
	public ResponseEntity<UsuarioRespuesta> crear(@RequestBody Map<String, String> cuerpo) {
		Rol rol = cuerpo.get("rol") == null ? null : Rol.valueOf(cuerpo.get("rol"));
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(servicio.crear(cuerpo.get("username"), cuerpo.get("nombre"), cuerpo.get("contrasena"), rol));
	}

	@PatchMapping("/{id}/rol")
	public ResponseEntity<UsuarioRespuesta> cambiarRol(@PathVariable Long id, @RequestBody Map<String, String> cuerpo) {
		Rol rol = cuerpo.get("rol") == null ? null : Rol.valueOf(cuerpo.get("rol"));
		return ResponseEntity.ok(servicio.cambiarRol(id, rol));
	}

	@PostMapping("/{id}/bloquear")
	public ResponseEntity<UsuarioRespuesta> bloquear(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.bloquear(id));
	}

	@PostMapping("/{id}/desbloquear")
	public ResponseEntity<UsuarioRespuesta> desbloquear(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.desbloquear(id));
	}

	@PostMapping("/{id}/reestablecer-contrasena")
	public ResponseEntity<UsuarioRespuesta> reestablecer(@PathVariable Long id, @RequestBody Map<String, String> cuerpo) {
		return ResponseEntity.ok(servicio.reestablecerContrasena(id, cuerpo.get("contrasena")));
	}
}