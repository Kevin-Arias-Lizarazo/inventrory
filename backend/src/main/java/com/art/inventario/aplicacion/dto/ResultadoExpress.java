package com.art.inventario.aplicacion.dto;

public class ResultadoExpress {

	private boolean ok;
	private String mensaje;
	private Long id;
	private String codigo;

	public ResultadoExpress() {
	}

	public ResultadoExpress(boolean ok, String mensaje, Long id, String codigo) {
		this.ok = ok;
		this.mensaje = mensaje;
		this.id = id;
		this.codigo = codigo;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
}
