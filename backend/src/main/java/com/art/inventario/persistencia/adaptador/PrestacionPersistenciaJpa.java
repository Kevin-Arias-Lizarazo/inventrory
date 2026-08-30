package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Prestacion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.PrestacionConsultaJpa;
import com.art.inventario.puerto.salida.PrestacionPersistencia;

@Repository
@Transactional(readOnly = true)
public class PrestacionPersistenciaJpa implements PrestacionPersistencia {

	private final PrestacionConsultaJpa consulta;

	public PrestacionPersistenciaJpa(PrestacionConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Prestacion> listar() {
		return MapeadorCatalogos.aDominioPrestaciones(consulta.findAll());
	}

	@Override
	public Optional<Prestacion> porNombre(String nombre) {
		return consulta.findByNombre(nombre).map(MapeadorCatalogos::aDominio);
	}

	@Override
	public Prestacion obtener(Long id) {
		return consulta.findById(id)
				.map(MapeadorCatalogos::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Prestación no encontrada"));
	}

	@Override
	@Transactional
	public Prestacion guardar(Prestacion prestacion) {
		return MapeadorCatalogos.aDominio(consulta.save(MapeadorCatalogos.aEntidad(prestacion)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Prestación no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		if (excluirId == null) {
			return consulta.existsByNombreIgnoreCase(nombre);
		}
		return consulta.existsByNombreIgnoreCaseAndIdNot(nombre, excluirId);
	}

	@Override
	public List<Prestacion> listarPorTipoContrato(Long tipoContratoId) {
		return MapeadorCatalogos.aDominioPrestaciones(consulta.findByTipoContratoId(tipoContratoId));
	}
}
