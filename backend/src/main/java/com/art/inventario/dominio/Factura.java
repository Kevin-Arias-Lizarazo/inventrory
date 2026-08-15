package com.art.inventario.dominio;

import java.util.ArrayList;
import java.util.List;

public final class Factura {

	private Long id;
	private String numero;
	private String fecha;
	private String observacion;
	private Proveedor proveedor;
	private Long compraId;
	private Double total;
	private boolean crearCompra;
	private List<LineaFactura> lineas = new ArrayList<>();

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

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
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

	public boolean isCrearCompra() {
		return crearCompra;
	}

	public void setCrearCompra(boolean crearCompra) {
		this.crearCompra = crearCompra;
	}

	public List<LineaFactura> getLineas() {
		return lineas;
	}

	public void setLineas(List<LineaFactura> lineas) {
		this.lineas = lineas;
	}
}
