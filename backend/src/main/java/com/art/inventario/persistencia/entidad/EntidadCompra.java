package com.art.inventario.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "compras")
public class EntidadCompra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fecha;
	private String observacion;
	private Long facturaId;

	@ManyToOne
	@JoinColumn(name = "proveedor_id")
	private EntidadProveedor proveedor;

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

	public Long getFacturaId() {
		return facturaId;
	}

	public void setFacturaId(Long facturaId) {
		this.facturaId = facturaId;
	}

	public EntidadProveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(EntidadProveedor proveedor) {
		this.proveedor = proveedor;
	}
}