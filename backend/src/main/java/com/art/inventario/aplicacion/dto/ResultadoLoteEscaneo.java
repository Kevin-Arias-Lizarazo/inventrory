package com.art.inventario.aplicacion.dto;

import java.util.ArrayList;
import java.util.List;

public class ResultadoLoteEscaneo {

	private TipoLoteEscaneo tipo;
	private String destinoCodigo;
	private boolean ok;
	private String mensaje;
	private int registrosCreados;
	private List<ItemErrorLote> errores = new ArrayList<>();
	private List<ItemPendienteLote> pendientes = new ArrayList<>();

	public ResultadoLoteEscaneo() {
	}

	public ResultadoLoteEscaneo(TipoLoteEscaneo tipo, String destinoCodigo, boolean ok, String mensaje,
			int registrosCreados) {
		this.tipo = tipo;
		this.destinoCodigo = destinoCodigo;
		this.ok = ok;
		this.mensaje = mensaje;
		this.registrosCreados = registrosCreados;
	}

	public TipoLoteEscaneo getTipo() {
		return tipo;
	}

	public void setTipo(TipoLoteEscaneo tipo) {
		this.tipo = tipo;
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

	public List<ItemErrorLote> getErrores() {
		return errores;
	}

	public void setErrores(List<ItemErrorLote> errores) {
		this.errores = errores;
	}

	public List<ItemPendienteLote> getPendientes() {
		return pendientes;
	}

	public void setPendientes(List<ItemPendienteLote> pendientes) {
		this.pendientes = pendientes;
	}
}
