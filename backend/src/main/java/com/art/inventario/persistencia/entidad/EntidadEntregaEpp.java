package com.art.inventario.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "entregas_epp")
public class EntidadEntregaEpp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fecha;
	private String observacion;
	private String fotoUrl;
	private String firmaUrl;

	@ManyToOne
	@JoinColumn(name = "empleado_id")
	private EntidadEmpleado empleado;

	@ManyToOne
	@JoinColumn(name = "epp_id")
	private EntidadEpp epp;

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

	public String getFotoUrl() {
		return fotoUrl;
	}

	public void setFotoUrl(String fotoUrl) {
		this.fotoUrl = fotoUrl;
	}

	public String getFirmaUrl() {
		return firmaUrl;
	}

	public void setFirmaUrl(String firmaUrl) {
		this.firmaUrl = firmaUrl;
	}

	public EntidadEmpleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(EntidadEmpleado empleado) {
		this.empleado = empleado;
	}

	public EntidadEpp getEpp() {
		return epp;
	}

	public void setEpp(EntidadEpp epp) {
		this.epp = epp;
	}
}