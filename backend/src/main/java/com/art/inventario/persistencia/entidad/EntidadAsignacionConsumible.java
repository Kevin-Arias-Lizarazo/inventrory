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
@Table(name = "asignaciones_consumibles")
public class EntidadAsignacionConsumible {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal cantidad;
	private String fecha;
	private String observacion;

	@ManyToOne
	@JoinColumn(name = "consumible_id")
	private EntidadConsumible consumible;

	@ManyToOne
	@JoinColumn(name = "proyecto_id")
	private EntidadProyecto proyecto;

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

	public EntidadConsumible getConsumible() {
		return consumible;
	}

	public void setConsumible(EntidadConsumible consumible) {
		this.consumible = consumible;
	}

	public EntidadProyecto getProyecto() {
		return proyecto;
	}

	public void setProyecto(EntidadProyecto proyecto) {
		this.proyecto = proyecto;
	}
}