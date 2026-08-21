package com.art.inventario.aplicacion.dto;

public class RespuestaInstalacion {

	private UsuarioRespuesta usuario;
	private String secretoRecuperacion;

	public RespuestaInstalacion() {
	}

	public RespuestaInstalacion(UsuarioRespuesta usuario, String secretoRecuperacion) {
		this.usuario = usuario;
		this.secretoRecuperacion = secretoRecuperacion;
	}

	public UsuarioRespuesta getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioRespuesta usuario) {
		this.usuario = usuario;
	}

	public String getSecretoRecuperacion() {
		return secretoRecuperacion;
	}

	public void setSecretoRecuperacion(String secretoRecuperacion) {
		this.secretoRecuperacion = secretoRecuperacion;
	}
}