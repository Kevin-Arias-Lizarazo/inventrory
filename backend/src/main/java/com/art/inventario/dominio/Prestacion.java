package com.art.inventario.dominio;

public final class Prestacion {

	public static final String TIPO_LABORAL = "LABORAL";
	public static final String TIPO_PRESTACION_SERVICIOS = "PRESTACION_SERVICIOS";
	public static final String TIPO_APRENDIZAJE = "APRENDIZAJE";

	private Long id;
	private String nombre;
	private String tipo;
	private Boolean obligatoria;
	private Boolean activo;

	public Prestacion() {
	}

	public Prestacion(String nombre, String tipo, Boolean obligatoria, Boolean activo) {
		this.nombre = nombre;
		this.tipo = tipo;
		this.obligatoria = obligatoria;
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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Boolean isObligatoria() {
		return obligatoria;
	}

	public void setObligatoria(Boolean obligatoria) {
		this.obligatoria = obligatoria;
	}

	public Boolean isActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
}
