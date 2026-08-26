package com.art.inventario.aplicacion.dto;

import java.math.BigDecimal;
import java.util.List;

public class ItemEscaneoLote {

	private String codigo;
	private BigDecimal cantidad;
	private List<DevolucionAsignacion> asignaciones;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public List<DevolucionAsignacion> getAsignaciones() {
		return asignaciones;
	}

	public void setAsignaciones(List<DevolucionAsignacion> asignaciones) {
		this.asignaciones = asignaciones;
	}
}
