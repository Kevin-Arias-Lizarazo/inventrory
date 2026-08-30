package com.art.inventario.dominio;

public final class TipoContratoPrestacion {

	private Long id;
	private Long tipoContratoId;
	private Long prestacionId;

	public TipoContratoPrestacion() {
	}

	public TipoContratoPrestacion(Long tipoContratoId, Long prestacionId) {
		this.tipoContratoId = tipoContratoId;
		this.prestacionId = prestacionId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTipoContratoId() {
		return tipoContratoId;
	}

	public void setTipoContratoId(Long tipoContratoId) {
		this.tipoContratoId = tipoContratoId;
	}

	public Long getPrestacionId() {
		return prestacionId;
	}

	public void setPrestacionId(Long prestacionId) {
		this.prestacionId = prestacionId;
	}
}
