package com.art.inventario.aplicacion.dto;

import java.util.ArrayList;
import java.util.List;

public class BloqueEscaneo {

	private String operacion;
	private String destinoCodigo;
	private List<ItemEscaneo> items = new ArrayList<>();

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

	public List<ItemEscaneo> getItems() {
		return items;
	}

	public void setItems(List<ItemEscaneo> items) {
		this.items = items;
	}
}