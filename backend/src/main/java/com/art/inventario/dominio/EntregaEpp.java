package com.art.inventario.dominio;

public final class EntregaEpp {

	private Long id;
	private String fecha;
	private String observacion;
	private String fotoUrl;
	private String firmaUrl;
	private String fechaVencimiento;
	private Empleado empleado;
	private Epp epp;

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

	public String getFotoUrl() {
		return fotoUrl;
	}

	public void setFotoUrl(String fotoUrl) {
		this.fotoUrl = fotoUrl;
	}

	public String getFirmaUrl() {
		return firmaUrl;
	}

	public void setFirmaUrl(String firmaUrl) {
		this.firmaUrl = firmaUrl;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}

	public Epp getEpp() {
		return epp;
	}

	public void setEpp(Epp epp) {
		this.epp = epp;
	}
	public String getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}
}