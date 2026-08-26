package com.art.inventario.aplicacion.dto;

import java.math.BigDecimal;

public class DevolucionAsignacion {

	private Long id;
	private BigDecimal cantidad;

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
}
