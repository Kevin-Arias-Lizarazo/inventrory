package com.art.inventario.dominio;

import java.util.ArrayList;
import java.util.List;

public final class Devolucion {

	private Long id;
	private String fecha;
	private String observacion;
	private Long compraId;
	private List<LineaDevolucion> lineas = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public Long getCompraId() {
		return compraId;
	}

	public void setCompraId(Long compraId) {
		this.compraId = compraId;
	}

	public List<LineaDevolucion> getLineas() {
		return lineas;
	}

	public void setLineas(List<LineaDevolucion> lineas) {
		this.lineas = lineas;
	}
}
