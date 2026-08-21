package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.dominio.Rol;

public interface UsuarioCasoDeUso {

	List<UsuarioRespuesta> listar();

	UsuarioRespuesta crear(String username, String nombre, String contrasena, Rol rol);

	UsuarioRespuesta cambiarRol(Long id, Rol rol);

	UsuarioRespuesta bloquear(Long id);

	UsuarioRespuesta desbloquear(Long id);

	UsuarioRespuesta reestablecerContrasena(Long id, String nuevaContrasena);
}