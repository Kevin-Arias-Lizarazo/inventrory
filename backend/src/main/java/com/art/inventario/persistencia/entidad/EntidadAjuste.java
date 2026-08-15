package com.art.inventario.persistencia.entidad;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ajustes")
public class EntidadAjuste {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fecha;
	private String observacion;
	private String motivo;

	@OneToMany(mappedBy = "ajuste", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EntidadLineaAjuste> lineas = new ArrayList<>();

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

	public List<EntidadLineaAjuste> getLineas() {
		return lineas;
	}

	public void setLineas(List<EntidadLineaAjuste> lineas) {
		this.lineas = lineas;
	}
}
