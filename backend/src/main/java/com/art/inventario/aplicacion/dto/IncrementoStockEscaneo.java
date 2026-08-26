package com.art.inventario.aplicacion.dto;

import java.math.BigDecimal;

public class IncrementoStockEscaneo {

	private String codigo;
	private BigDecimal cantidad;

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
}
