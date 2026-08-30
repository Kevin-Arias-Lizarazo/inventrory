package com.art.inventario.dominio;

import java.math.BigDecimal;

/**
 * Immutable snapshot line of a calculated benefit (prestación) for a contract.
 *
 * <p>Every field is a literal copy taken at calculation time ({@code snap_dato}):
 * there is no reference back to the catalogs. Changing a legal parameter never
 * mutates existing rows; only a recalculation replaces them (delete + insert).
 */
public final class ContratoPrestacionCalculada {

	private Long id;
	private Long contratoId;
	private String concepto;
	private String tipo;
	private String quienPaga;
	private BigDecimal base;
	private BigDecimal porcentaje;
	private BigDecimal valorMensual;
	private BigDecimal valorAnual;
	private Boolean obligatoria;
	private String fechaCalculo;

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

	public String getQuienPaga() {
		return quienPaga;
	}

	public void setQuienPaga(String quienPaga) {
		this.quienPaga = quienPaga;
	}

	public BigDecimal getBase() {
		return base;
	}

	public void setBase(BigDecimal base) {
		this.base = base;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	public BigDecimal getValorMensual() {
		return valorMensual;
	}

	public void setValorMensual(BigDecimal valorMensual) {
		this.valorMensual = valorMensual;
	}

	public BigDecimal getValorAnual() {
		return valorAnual;
	}

	public void setValorAnual(BigDecimal valorAnual) {
		this.valorAnual = valorAnual;
	}

	public Boolean getObligatoria() {
		return obligatoria;
	}

	public void setObligatoria(Boolean obligatoria) {
		this.obligatoria = obligatoria;
	}

	public String getFechaCalculo() {
		return fechaCalculo;
	}

	public void setFechaCalculo(String fechaCalculo) {
		this.fechaCalculo = fechaCalculo;
	}
}
