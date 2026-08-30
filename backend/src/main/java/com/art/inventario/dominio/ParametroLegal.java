package com.art.inventario.dominio;

import java.math.BigDecimal;

public final class ParametroLegal {

	private Long id;
	private int anio;
	private BigDecimal smlmv;
	private BigDecimal auxilioTransporte;
	private BigDecimal porcentajeSalud;
	private BigDecimal porcentajePension;
	private BigDecimal porcentajeArl;
	private BigDecimal porcentajeCaja;
	private BigDecimal porcentajeSena;
	private BigDecimal porcentajeIcbf;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getAnio() {
		return anio;
	}

	public void setAnio(int anio) {
		this.anio = anio;
	}

	public BigDecimal getSmlmv() {
		return smlmv;
	}

	public void setSmlmv(BigDecimal smlmv) {
		this.smlmv = smlmv;
	}

	public BigDecimal getAuxilioTransporte() {
		return auxilioTransporte;
	}

	public void setAuxilioTransporte(BigDecimal auxilioTransporte) {
		this.auxilioTransporte = auxilioTransporte;
	}

	public BigDecimal getPorcentajeSalud() {
		return porcentajeSalud;
	}

	public void setPorcentajeSalud(BigDecimal porcentajeSalud) {
		this.porcentajeSalud = porcentajeSalud;
	}

	public BigDecimal getPorcentajePension() {
		return porcentajePension;
	}

	public void setPorcentajePension(BigDecimal porcentajePension) {
		this.porcentajePension = porcentajePension;
	}

	public BigDecimal getPorcentajeArl() {
		return porcentajeArl;
	}

	public void setPorcentajeArl(BigDecimal porcentajeArl) {
		this.porcentajeArl = porcentajeArl;
	}

	public BigDecimal getPorcentajeCaja() {
		return porcentajeCaja;
	}

	public void setPorcentajeCaja(BigDecimal porcentajeCaja) {
		this.porcentajeCaja = porcentajeCaja;
	}

	public BigDecimal getPorcentajeSena() {
		return porcentajeSena;
	}

	public void setPorcentajeSena(BigDecimal porcentajeSena) {
		this.porcentajeSena = porcentajeSena;
	}

	public BigDecimal getPorcentajeIcbf() {
		return porcentajeIcbf;
	}

	public void setPorcentajeIcbf(BigDecimal porcentajeIcbf) {
		this.porcentajeIcbf = porcentajeIcbf;
	}
}
