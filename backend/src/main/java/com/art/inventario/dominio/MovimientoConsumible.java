package com.art.inventario.dominio;

public final class MovimientoConsumible {

	private Long id;
	private String tipo;
	private Integer cantidad;
	private String fecha;
	private String observacion;
	private Consumible consumible;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
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
}