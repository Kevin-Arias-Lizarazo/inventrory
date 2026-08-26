package com.art.inventario.aplicacion.dto;

public class ItemPendienteLote {

	private String codigo;
	private String motivo;
	private String mensaje;

	public ItemPendienteLote() {
	}

	public ItemPendienteLote(String codigo, String motivo, String mensaje) {
		this.codigo = codigo;
		this.motivo = motivo;
		this.mensaje = mensaje;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}
