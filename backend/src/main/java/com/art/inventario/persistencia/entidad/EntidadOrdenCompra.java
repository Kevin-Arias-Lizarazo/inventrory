package com.art.inventario.persistencia.entidad;

import java.util.*;
import jakarta.persistence.*;

@Entity
@Table(name = "ordenes_compra")
public class EntidadOrdenCompra {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String fecha;
	private String observacion;
	private Double total;
	@ManyToOne @JoinColumn(name = "proveedor_id")
	private EntidadProveedor proveedor;
	@OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EntidadLineaOrdenCompra> lineas = new ArrayList<>();
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getFecha() { return fecha; }
	public void setFecha(String fecha) { this.fecha = fecha; }
	public String getObservacion() { return observacion; }
	public void setObservacion(String observacion) { this.observacion = observacion; }
	public Double getTotal() { return total; }
	public void setTotal(Double total) { this.total = total; }
	public EntidadProveedor getProveedor() { return proveedor; }
	public void setProveedor(EntidadProveedor proveedor) { this.proveedor = proveedor; }
	public List<EntidadLineaOrdenCompra> getLineas() { return lineas; }
	public void setLineas(List<EntidadLineaOrdenCompra> lineas) { this.lineas = lineas; }
}
