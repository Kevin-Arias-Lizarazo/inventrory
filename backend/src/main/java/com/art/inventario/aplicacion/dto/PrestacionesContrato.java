package com.art.inventario.aplicacion.dto;

import java.math.BigDecimal;
import java.util.List;

import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.dominio.ContratoPrestacionExtra;

/**
 * Response payload for GET /api/contratos/{id}/prestaciones: the immutable
 * calculated snapshot, the manually-managed extras and the total employer cost.
 */
public class PrestacionesContrato {

	private List<ContratoPrestacionCalculada> calculadas;
	private List<ContratoPrestacionExtra> extras;
	private BigDecimal totalEmpleador;

	public PrestacionesContrato() {
	}

	public PrestacionesContrato(List<ContratoPrestacionCalculada> calculadas,
			List<ContratoPrestacionExtra> extras, BigDecimal totalEmpleador) {
		this.calculadas = calculadas;
		this.extras = extras;
		this.totalEmpleador = totalEmpleador;
	}

	public List<ContratoPrestacionCalculada> getCalculadas() {
		return calculadas;
	}

	public void setCalculadas(List<ContratoPrestacionCalculada> calculadas) {
		this.calculadas = calculadas;
	}

	public List<ContratoPrestacionExtra> getExtras() {
		return extras;
	}

	public void setExtras(List<ContratoPrestacionExtra> extras) {
		this.extras = extras;
	}

	public BigDecimal getTotalEmpleador() {
		return totalEmpleador;
	}

	public void setTotalEmpleador(BigDecimal totalEmpleador) {
		this.totalEmpleador = totalEmpleador;
	}
}
