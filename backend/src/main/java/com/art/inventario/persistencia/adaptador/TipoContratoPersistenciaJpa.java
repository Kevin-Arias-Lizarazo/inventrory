package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.TipoContratoConsultaJpa;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;

@Repository
@Transactional(readOnly = true)
public class TipoContratoPersistenciaJpa implements TipoContratoPersistencia {

	private final TipoContratoConsultaJpa consulta;

	public TipoContratoPersistenciaJpa(TipoContratoConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<TipoContrato> listar() {
		return MapeadorCatalogos.aDominioTiposContrato(consulta.findAll());
	}

	@Override
	public Optional<TipoContrato> porNombre(String nombre) {
		return consulta.findByNombre(nombre).map(MapeadorCatalogos::aDominio);
	}

	@Override
	public TipoContrato obtener(Long id) {
		return consulta.findById(id)
				.map(MapeadorCatalogos::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Tipo de contrato no encontrado"));
	}

	@Override
	@Transactional
	public TipoContrato guardar(TipoContrato tipoContrato) {
		return MapeadorCatalogos.aDominio(consulta.save(MapeadorCatalogos.aEntidad(tipoContrato)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Tipo de contrato no encontrado");
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
}
