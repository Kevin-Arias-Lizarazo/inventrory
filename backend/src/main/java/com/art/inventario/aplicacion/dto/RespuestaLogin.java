package com.art.inventario.aplicacion.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RespuestaLogin {

	private String accessToken;

	@JsonIgnore
	private String refreshToken;

	private UsuarioRespuesta usuario;

	public RespuestaLogin() {
	}

	public RespuestaLogin(String accessToken, UsuarioRespuesta usuario) {
		this.accessToken = accessToken;
		this.usuario = usuario;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public UsuarioRespuesta getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioRespuesta usuario) {
		this.usuario = usuario;
	}
}