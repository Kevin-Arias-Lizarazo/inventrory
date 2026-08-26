package com.art.inventario.aplicacion.dto;

import java.math.BigDecimal;

public class CreacionExpressEscaneo {

	private TipoProductoExpress tipo;
	private String codigo;
	private String nombre;
	private String marca;
	private String unidad;
	private Integer cantidadTotal;

	public TipoProductoExpress getTipo() {
		return tipo;
	}

	public void setTipo(TipoProductoExpress tipo) {
		this.tipo = tipo;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public Integer getCantidadTotal() {
		return cantidadTotal;
	}

	public void setCantidadTotal(Integer cantidadTotal) {
		this.cantidadTotal = cantidadTotal;
	}
}
