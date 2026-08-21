package com.art.inventario.configuracion;

import com.art.inventario.dominio.Rol;

public class UsuarioAutenticado {

	private final Long id;
	private final String username;
	private final String nombre;
	private final Rol rol;

	public UsuarioAutenticado(Long id, String username, String nombre, Rol rol) {
		this.id = id;
		this.username = username;
		this.nombre = nombre;
		this.rol = rol;
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

	public Rol getRol() {
		return rol;
	}

	public boolean esRoot() {
		return "root".equalsIgnoreCase(username);
	}
}