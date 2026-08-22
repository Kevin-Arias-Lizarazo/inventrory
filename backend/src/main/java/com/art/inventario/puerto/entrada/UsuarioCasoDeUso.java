package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.UsuarioRespuesta;

public interface UsuarioCasoDeUso {

	List<UsuarioRespuesta> listar();

	UsuarioRespuesta crear(String username, String nombre, String contrasena, String nivel);

	UsuarioRespuesta cambiarNivel(Long id, String nivel);

	UsuarioRespuesta bloquear(Long id);

	UsuarioRespuesta desbloquear(Long id);
}