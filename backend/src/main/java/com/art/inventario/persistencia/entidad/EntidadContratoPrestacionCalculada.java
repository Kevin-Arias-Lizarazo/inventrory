package com.art.inventario.persistencia.entidad;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for a calculated benefit snapshot (contrato_prestacion_calculada).
 *
 * <p>All values are literal copies taken at calculation time. {@code contratoId}
 * is an ordinary column, intentionally NOT a foreign key: the snapshot remains
 * immutable even if the linked catalogs or contract change. Recalculation
 * replaces the whole set of rows for a contract (delete + insert).
 */
@Entity
@Table(name = "contrato_prestacion_calculada")
public class EntidadContratoPrestacionCalculada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "contrato_id", nullable = false)
	private Long contratoId;

	private String concepto;
	private String tipo;
	private String quienPaga;
	private BigDecimal base;
	private BigDecimal porcentaje;
	private BigDecimal valorMensual;
	private BigDecimal valorAnual;
	private Boolean obligatoria;

	@Column(name = "fecha_calculo")
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
