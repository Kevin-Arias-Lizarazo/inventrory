package com.art.inventario.persistencia.entidad;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "contratos")
public class EntidadContrato {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fechaInicio;
	private String fechaFin;
	private String estado;

	@ManyToOne
	@JoinColumn(name = "empleado_id")
	private EntidadEmpleado empleado;

	@ManyToOne
	@JoinColumn(name = "tipo_contrato_id")
	private EntidadTipoContrato tipoContrato;

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

	public EntidadEmpleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(EntidadEmpleado empleado) {
		this.empleado = empleado;
	}

	public EntidadTipoContrato getTipoContrato() {
		return tipoContrato;
	}

	public void setTipoContrato(EntidadTipoContrato tipoContrato) {
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