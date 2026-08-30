package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.TipoContratoPrestacion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.PrestacionConsultaJpa;
import com.art.inventario.persistencia.consulta.TipoContratoConsultaJpa;
import com.art.inventario.persistencia.consulta.TipoContratoPrestacionConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadPrestacion;
import com.art.inventario.persistencia.entidad.EntidadTipoContrato;
import com.art.inventario.puerto.salida.TipoContratoPrestacionPersistencia;

@Repository
@Transactional(readOnly = true)
public class TipoContratoPrestacionPersistenciaJpa implements TipoContratoPrestacionPersistencia {

	private final TipoContratoPrestacionConsultaJpa consulta;
	private final TipoContratoConsultaJpa tipoContratoConsulta;
	private final PrestacionConsultaJpa prestacionConsulta;

	public TipoContratoPrestacionPersistenciaJpa(TipoContratoPrestacionConsultaJpa consulta,
			TipoContratoConsultaJpa tipoContratoConsulta, PrestacionConsultaJpa prestacionConsulta) {
		this.consulta = consulta;
		this.tipoContratoConsulta = tipoContratoConsulta;
		this.prestacionConsulta = prestacionConsulta;
	}

	@Override
	public List<TipoContratoPrestacion> listar() {
		return MapeadorCatalogos.aDominioRelaciones(consulta.findAll());
	}

	@Override
	public List<TipoContratoPrestacion> listarPorTipoContrato(Long tipoContratoId) {
		return MapeadorCatalogos.aDominioRelaciones(consulta.findByTipoContratoId(tipoContratoId));
	}

	@Override
	@Transactional
	public TipoContratoPrestacion guardar(TipoContratoPrestacion relacion) {
		EntidadTipoContrato tipo = tipoContratoConsulta.findById(relacion.getTipoContratoId())
				.orElseThrow(() -> new NoEncontradoExcepcion("Tipo de contrato no encontrado"));
		EntidadPrestacion prestacion = prestacionConsulta.findById(relacion.getPrestacionId())
				.orElseThrow(() -> new NoEncontradoExcepcion("Prestación no encontrada"));
		return MapeadorCatalogos.aDominio(
				consulta.save(MapeadorCatalogos.aEntidad(relacion, tipo, prestacion)));
	}

	@Override
	@Transactional
	public void eliminarPorTipoContrato(Long tipoContratoId) {
		consulta.eliminarPorTipoContrato(tipoContratoId);
	}
}
