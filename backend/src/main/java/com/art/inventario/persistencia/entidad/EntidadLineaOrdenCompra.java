package com.art.inventario.persistencia.entidad;

import jakarta.persistence.*;

@Entity
@Table(name = "lineas_ordenes_compra")
public class EntidadLineaOrdenCompra {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne @JoinColumn(name = "orden_id")
	private EntidadOrdenCompra orden;
	private String tipo;
	private Long productoId;
	private String descripcion;
	private Integer cantidad;
	private Double costoUnitario;
	private Double subtotal;
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public EntidadOrdenCompra getOrden() { return orden; }
	public void setOrden(EntidadOrdenCompra orden) { this.orden = orden; }
	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }
	public Long getProductoId() { return productoId; }
	public void setProductoId(Long productoId) { this.productoId = productoId; }
	public String getDescripcion() { return descripcion; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public Integer getCantidad() { return cantidad; }
	public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
	public Double getCostoUnitario() { return costoUnitario; }
	public void setCostoUnitario(Double costoUnitario) { this.costoUnitario = costoUnitario; }
	public Double getSubtotal() { return subtotal; }
	public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
