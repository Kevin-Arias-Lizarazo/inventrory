package com.art.inventario.aplicacion.dto;

public class ResultadoBusqueda {

	private String recurso;
	private Long id;
	private String etiqueta;

	public ResultadoBusqueda() {
	}

	public ResultadoBusqueda(String recurso, Long id, String etiqueta) {
		this.recurso = recurso;
		this.id = id;
		this.etiqueta = etiqueta;
	}

	public String getRecurso() {
		return recurso;
	}

	public void setRecurso(String recurso) {
		this.recurso = recurso;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}
}