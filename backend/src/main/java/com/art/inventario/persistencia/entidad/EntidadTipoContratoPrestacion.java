package com.art.inventario.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_contrato_prestacion")
public class EntidadTipoContratoPrestacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "tipo_contrato_id")
	private EntidadTipoContrato tipoContrato;

	@ManyToOne
	@JoinColumn(name = "prestacion_id")
	private EntidadPrestacion prestacion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public EntidadTipoContrato getTipoContrato() {
		return tipoContrato;
	}

	public void setTipoContrato(EntidadTipoContrato tipoContrato) {
		this.tipoContrato = tipoContrato;
	}

	public EntidadPrestacion getPrestacion() {
		return prestacion;
	}

	public void setPrestacion(EntidadPrestacion prestacion) {
		this.prestacion = prestacion;
	}
}
