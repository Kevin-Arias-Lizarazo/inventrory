package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.ContratoPrestacionExtra;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.ContratoPrestacionExtraConsultaJpa;
import com.art.inventario.puerto.salida.ContratoPrestacionExtraPersistencia;

@Repository
@Transactional(readOnly = true)
public class ContratoPrestacionExtraPersistenciaJpa implements ContratoPrestacionExtraPersistencia {

	private final ContratoPrestacionExtraConsultaJpa consulta;

	public ContratoPrestacionExtraPersistenciaJpa(ContratoPrestacionExtraConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<ContratoPrestacionExtra> listarPorContrato(Long contratoId) {
		return MapeadorCatalogos.aDominioExtras(consulta.findByContratoId(contratoId));
	}

	@Override
	public ContratoPrestacionExtra obtener(Long id) {
		return consulta.findById(id)
				.map(MapeadorCatalogos::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Prestación extra no encontrada"));
	}

	@Override
	@Transactional
	public ContratoPrestacionExtra guardar(ContratoPrestacionExtra extra) {
		return MapeadorCatalogos.aDominio(consulta.save(MapeadorCatalogos.aEntidad(extra)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Prestación extra no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean existe(Long id) {
		return consulta.existsById(id);
	}
}
