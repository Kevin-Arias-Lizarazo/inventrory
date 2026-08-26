package com.art.inventario.dominio;

import java.math.BigDecimal;

public final class AsignacionConsumible {

	private Long id;
	private BigDecimal cantidad;
	private String fecha;
	private String observacion;
	private Consumible consumible;
	private Proyecto proyecto;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
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

	public Consumible getConsumible() {
		return consumible;
	}

	public void setConsumible(Consumible consumible) {
		this.consumible = consumible;
	}

	public Proyecto getProyecto() {
		return proyecto;
	}

	public void setProyecto(Proyecto proyecto) {
		this.proyecto = proyecto;
	}
}