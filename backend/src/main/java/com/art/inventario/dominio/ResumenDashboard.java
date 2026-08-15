package com.art.inventario.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ResumenDashboard {

	private Double valorInventario;
	private Integer productosSinCosto;
	private Integer alertasReposicion;
	private Integer alertasVencimientoEpp;
	private Integer totalProductos;
	private Double gastoFacturasRango;
	private Integer comprasRango;
	private List<Map<String, Object>> valorPorCategoria = new ArrayList<>();

	public Double getValorInventario() {
		return valorInventario;
	}

	public void setValorInventario(Double valorInventario) {
		this.valorInventario = valorInventario;
	}

	public Integer getProductosSinCosto() {
		return productosSinCosto;
	}

	public void setProductosSinCosto(Integer productosSinCosto) {
		this.productosSinCosto = productosSinCosto;
	}

	public Integer getAlertasReposicion() {
		return alertasReposicion;
	}

	public void setAlertasReposicion(Integer alertasReposicion) {
		this.alertasReposicion = alertasReposicion;
	}

	public Integer getAlertasVencimientoEpp() {
		return alertasVencimientoEpp;
	}

	public void setAlertasVencimientoEpp(Integer alertasVencimientoEpp) {
		this.alertasVencimientoEpp = alertasVencimientoEpp;
	}

	public Integer getTotalProductos() {
		return totalProductos;
	}

	public void setTotalProductos(Integer totalProductos) {
		this.totalProductos = totalProductos;
	}

	public Double getGastoFacturasRango() {
		return gastoFacturasRango;
	}

	public void setGastoFacturasRango(Double gastoFacturasRango) {
		this.gastoFacturasRango = gastoFacturasRango;
	}

	public Integer getComprasRango() {
		return comprasRango;
	}

	public void setComprasRango(Integer comprasRango) {
		this.comprasRango = comprasRango;
	}

	public List<Map<String, Object>> getValorPorCategoria() {
		return valorPorCategoria;
	}

	public void setValorPorCategoria(List<Map<String, Object>> valorPorCategoria) {
		this.valorPorCategoria = valorPorCategoria;
	}
}
