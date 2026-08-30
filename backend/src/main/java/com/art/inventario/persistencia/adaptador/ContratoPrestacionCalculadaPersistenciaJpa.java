package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.ContratoPrestacionCalculadaConsultaJpa;
import com.art.inventario.puerto.salida.ContratoPrestacionCalculadaPersistencia;

@Repository
@Transactional(readOnly = true)
public class ContratoPrestacionCalculadaPersistenciaJpa
		implements ContratoPrestacionCalculadaPersistencia {

	private final ContratoPrestacionCalculadaConsultaJpa consulta;

	public ContratoPrestacionCalculadaPersistenciaJpa(ContratoPrestacionCalculadaConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<ContratoPrestacionCalculada> listarPorContrato(Long contratoId) {
		return MapeadorCatalogos.aDominioCalculadas(consulta.findByContratoId(contratoId));
	}

	@Override
	@Transactional
	public void guardar(ContratoPrestacionCalculada linea) {
		consulta.save(MapeadorCatalogos.aEntidad(linea));
	}

	@Override
	@Transactional
	public void eliminarPorContrato(Long contratoId) {
		consulta.deleteByContratoId(contratoId);
	}
}
