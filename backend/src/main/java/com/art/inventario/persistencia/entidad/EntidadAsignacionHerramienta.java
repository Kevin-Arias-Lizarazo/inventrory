package com.art.inventario.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignaciones_herramientas")
public class EntidadAsignacionHerramienta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String lugar;
	private String fecha;
	private Boolean devuelta;
	private String fechaDevolucion;
	private Integer cantidad;

	@ManyToOne
	@JoinColumn(name = "empleado_id")
	private EntidadEmpleado empleado;

	@ManyToOne
	@JoinColumn(name = "herramienta_id")
	private EntidadHerramienta herramienta;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLugar() {
		return lugar;
	}

	public void setLugar(String lugar) {
		this.lugar = lugar;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public Boolean getDevuelta() {
		return devuelta;
	}

	public void setDevuelta(Boolean devuelta) {
		this.devuelta = devuelta;
	}

	public String getFechaDevolucion() {
		return fechaDevolucion;
	}

	public void setFechaDevolucion(String fechaDevolucion) {
		this.fechaDevolucion = fechaDevolucion;
	}

	public EntidadEmpleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(EntidadEmpleado empleado) {
		this.empleado = empleado;
	}

	public EntidadHerramienta getHerramienta() {
		return herramienta;
	}

	public void setHerramienta(EntidadHerramienta herramienta) {
		this.herramienta = herramienta;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
}