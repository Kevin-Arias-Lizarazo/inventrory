package com.art.inventario.aplicacion.dto;

import java.util.List;

public class LoteEscaneo {

	private TipoLoteEscaneo tipo;
	private String destinoCodigo;
	private List<ItemEscaneoLote> items;

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

	public List<ItemEscaneoLote> getItems() {
		return items;
	}

	public void setItems(List<ItemEscaneoLote> items) {
		this.items = items;
	}
}
