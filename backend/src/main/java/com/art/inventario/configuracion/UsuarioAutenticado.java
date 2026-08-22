package com.art.inventario.configuracion;

public class UsuarioAutenticado {

	private final Long id;
	private final String username;
	private final String nombre;
	private final String nivel;

	public UsuarioAutenticado(Long id, String username, String nombre, String nivel) {
		this.id = id;
		this.username = username;
		this.nombre = nombre;
		this.nivel = nivel;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getNombre() {
		return nombre;
	}

	public String getNivel() {
		return nivel;
	}

	public boolean esRoot() {
		return "ROOT".equalsIgnoreCase(nivel);
	}
}