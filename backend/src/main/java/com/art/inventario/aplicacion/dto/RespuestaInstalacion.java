package com.art.inventario.aplicacion.dto;

public class RespuestaInstalacion {

	private UsuarioRespuesta usuario;
	private String secretoRecuperacion;
	private String adminPasswordTemporal;

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

	public String getAdminPasswordTemporal() {
		return adminPasswordTemporal;
	}

	public void setAdminPasswordTemporal(String adminPasswordTemporal) {
		this.adminPasswordTemporal = adminPasswordTemporal;
	}
}