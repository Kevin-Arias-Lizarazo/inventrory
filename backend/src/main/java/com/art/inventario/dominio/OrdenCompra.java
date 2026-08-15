package com.art.inventario.dominio;

import java.util.ArrayList;
import java.util.List;

public final class OrdenCompra {

	private Long id;
	private String fecha;
	private String observacion;
	private Proveedor proveedor;
	private Double total;
	private List<LineaOrdenCompra> lineas = new ArrayList<>();

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

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public List<LineaOrdenCompra> getLineas() {
		return lineas;
	}

	public void setLineas(List<LineaOrdenCompra> lineas) {
		this.lineas = lineas;
	}
}
