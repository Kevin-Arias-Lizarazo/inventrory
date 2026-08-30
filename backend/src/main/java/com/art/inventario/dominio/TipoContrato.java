package com.art.inventario.dominio;

public final class TipoContrato {

	private Long id;
	private String nombre;
	private String descripcion;
	private Boolean activo;

	public TipoContrato() {
	}

	public TipoContrato(String nombre, String descripcion, Boolean activo) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.activo = activo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Boolean isActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
}
