package com.art.inventario.dominio;

import java.math.BigDecimal;

/**
 * An extra, manually-managed payment associated with a contract (for example
 * viáticos EVENTUAL with a date and amount, or primas RECURRENTES with a
 * validity window). These are independent of the snapshot calculation.
 */
public final class ContratoPrestacionExtra {

	public static final String TIPO_RECURRENTE = "RECURRENTE";
	public static final String TIPO_EVENTUAL = "EVENTUAL";

	private Long id;
	private Long contratoId;
	private String concepto;
	private String tipo;
	private BigDecimal valor;
	private String fecha;
	private String vigenciaDesde;
	private String vigenciaHasta;
	private String observacion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getContratoId() {
		return contratoId;
	}

	public void setContratoId(Long contratoId) {
		this.contratoId = contratoId;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getVigenciaDesde() {
		return vigenciaDesde;
	}

	public void setVigenciaDesde(String vigenciaDesde) {
		this.vigenciaDesde = vigenciaDesde;
	}

	public String getVigenciaHasta() {
		return vigenciaHasta;
	}

	public void setVigenciaHasta(String vigenciaHasta) {
		this.vigenciaHasta = vigenciaHasta;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
}
