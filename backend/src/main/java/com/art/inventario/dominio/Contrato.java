package com.art.inventario.dominio;

import java.math.BigDecimal;

public final class Contrato {

	public static final String ACTIVO = "ACTIVO";
	public static final String CONCLUIDO = "CONCLUIDO";

	public static final String FASE_LECTIVA = "LECTIVA";
	public static final String FASE_PRACTICA = "PRACTICA";

	private Long id;
	private String fechaInicio;
	private String fechaFin;
	private String estado;
	private Empleado empleado;
	private TipoContrato tipoContrato;
	private BigDecimal remuneracionMensual;
	private String faseAprendizaje;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}

	public TipoContrato getTipoContrato() {
		return tipoContrato;
	}

	public void setTipoContrato(TipoContrato tipoContrato) {
		this.tipoContrato = tipoContrato;
	}

	public BigDecimal getRemuneracionMensual() {
		return remuneracionMensual;
	}

	public void setRemuneracionMensual(BigDecimal remuneracionMensual) {
		this.remuneracionMensual = remuneracionMensual;
	}

	public String getFaseAprendizaje() {
		return faseAprendizaje;
	}

	public void setFaseAprendizaje(String faseAprendizaje) {
		this.faseAprendizaje = faseAprendizaje;
	}
}