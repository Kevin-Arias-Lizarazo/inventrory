package com.art.inventario.dominio;

import java.util.ArrayList;
import java.util.List;

public final class Compra {

	public static final String TIPO_HERRAMIENTA = "HERRAMIENTA";
	public static final String TIPO_EPP = "EPP";
	public static final String TIPO_CONSUMIBLE = "CONSUMIBLE";
	public static final String TIPO_MATERIAL = "MATERIAL";
	public static final String TIPO_ROPA = "ROPA";

	private Long id;
	private String fecha;
	private String observacion;
	private Proveedor proveedor;
	private Long facturaId;
	private List<LineaCompra> lineas = new ArrayList<>();

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

	public Long getFacturaId() {
		return facturaId;
	}

	public void setFacturaId(Long facturaId) {
		this.facturaId = facturaId;
	}

	public boolean facturada() {
		return facturaId != null;
	}

	public List<LineaCompra> getLineas() {
		return lineas;
	}

	public void setLineas(List<LineaCompra> lineas) {
		this.lineas = lineas;
	}
}
