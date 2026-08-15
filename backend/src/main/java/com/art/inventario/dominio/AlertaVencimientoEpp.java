package com.art.inventario.dominio;

public final class AlertaVencimientoEpp {

	public static final String TIPO_EPP = "EPP";
	public static final String TIPO_ENTREGA = "ENTREGA";

	private String tipo;
	private Long id;
	private String nombre;
	private String descripcion;
	private String fechaVencimiento;
	private Long diasRestantes;
	private String empleadoNombre;

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Long getDiasRestantes() {
		return diasRestantes;
	}

	public void setDiasRestantes(Long diasRestantes) {
		this.diasRestantes = diasRestantes;
	}

	public String getEmpleadoNombre() {
		return empleadoNombre;
	}

	public void setEmpleadoNombre(String empleadoNombre) {
		this.empleadoNombre = empleadoNombre;
	}
}
