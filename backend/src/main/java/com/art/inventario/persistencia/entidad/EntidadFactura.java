package com.art.inventario.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas")
public class EntidadFactura {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String numero;
	private String fecha;
	private String observacion;
	private Long compraId;
	private Double total;

	@ManyToOne
	@JoinColumn(name = "proveedor_id")
	private EntidadProveedor proveedor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
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

	public Long getCompraId() {
		return compraId;
	}

	public void setCompraId(Long compraId) {
		this.compraId = compraId;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public EntidadProveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(EntidadProveedor proveedor) {
		this.proveedor = proveedor;
	}
}