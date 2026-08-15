package com.art.inventario.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "minutas")
public class EntidadMinuta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String hora;
	private String fecha;

	@ManyToOne
	@JoinColumn(name = "empleado_id")
	private EntidadEmpleado empleado;

	@ManyToOne
	@JoinColumn(name = "proyecto_id")
	private EntidadProyecto proyecto;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHora() {
		return hora;
	}

	public void setHora(String hora) {
		this.hora = hora;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public EntidadEmpleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(EntidadEmpleado empleado) {
		this.empleado = empleado;
	}

	public EntidadProyecto getProyecto() {
		return proyecto;
	}

	public void setProyecto(EntidadProyecto proyecto) {
		this.proyecto = proyecto;
	}
}