package com.art.inventario.dominio;

import java.util.ArrayList;
import java.util.List;

public final class Ajuste {

	public static final String MOTIVO_CONTEO = "CONTEO";
	public static final String MOTIVO_MERMA = "MERMA";
	public static final String MOTIVO_SOBRANTE = "SOBRANTE";
	public static final String MOTIVO_DANO = "DANO";

	public static final String TIPO_HERRAMIENTA = "HERRAMIENTA";
	public static final String TIPO_EPP = "EPP";
	public static final String TIPO_CONSUMIBLE = "CONSUMIBLE";
	public static final String TIPO_MATERIAL = "MATERIAL";

	public static final String MOV_INGRESO = "INGRESO";
	public static final String MOV_EGRESO = "EGRESO";

	private Long id;
	private String fecha;
	private String observacion;
	private String motivo;
	private List<LineaAjuste> lineas = new ArrayList<>();

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

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public List<LineaAjuste> getLineas() {
		return lineas;
	}

	public void setLineas(List<LineaAjuste> lineas) {
		this.lineas = lineas;
	}
}
