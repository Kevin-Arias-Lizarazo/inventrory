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
	private Double totalPagado;
	private Double saldo;
	private String estadoPago;
	private List<LineaFactura> lineas = new ArrayList<>();
	private List<PagoFactura> pagos = new ArrayList<>();

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

	public Double getTotalPagado() {
		return totalPagado;
	}

	public void setTotalPagado(Double totalPagado) {
		this.totalPagado = totalPagado;
	}

	public Double getSaldo() {
		return saldo;
	}

	public void setSaldo(Double saldo) {
		this.saldo = saldo;
	}

	public String getEstadoPago() {
		return estadoPago;
	}

	public void setEstadoPago(String estadoPago) {
		this.estadoPago = estadoPago;
	}

	public List<PagoFactura> getPagos() {
		return pagos;
	}

	public void setPagos(List<PagoFactura> pagos) {
		this.pagos = pagos;
	}
}
