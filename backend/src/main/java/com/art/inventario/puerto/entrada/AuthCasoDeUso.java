package com.art.inventario.puerto.entrada;

import com.art.inventario.aplicacion.dto.RespuestaLogin;
import com.art.inventario.aplicacion.dto.UsuarioRespuesta;

public interface AuthCasoDeUso {

	RespuestaLogin login(String username, String password, String ip);

	void logout(String refreshToken);

	RespuestaLogin renovar(String refreshToken);

	UsuarioRespuesta usuarioActual();

	void cambiarContrasena(String username, String contrasenaActual, String nuevaContrasena);

	void recuperarAdmin(String secretoRoot, String nuevaContrasenaAdmin);
}