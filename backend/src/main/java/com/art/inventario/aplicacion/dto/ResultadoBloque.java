package com.art.inventario.aplicacion.dto;

public class ResultadoBloque {

	private String operacion;
	private String destinoCodigo;
	private boolean ok;
	private String mensaje;
	private int registrosCreados;

	public ResultadoBloque() {
	}

	public ResultadoBloque(String operacion, String destinoCodigo, boolean ok, String mensaje, int registrosCreados) {
		this.operacion = operacion;
		this.destinoCodigo = destinoCodigo;
		this.ok = ok;
		this.mensaje = mensaje;
		this.registrosCreados = registrosCreados;
	}

	public String getOperacion() {
		return operacion;
	}

	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	public String getDestinoCodigo() {
		return destinoCodigo;
	}

	public void setDestinoCodigo(String destinoCodigo) {
		this.destinoCodigo = destinoCodigo;
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

	public int getRegistrosCreados() {
		return registrosCreados;
	}

	public void setRegistrosCreados(int registrosCreados) {
		this.registrosCreados = registrosCreados;
	}
}