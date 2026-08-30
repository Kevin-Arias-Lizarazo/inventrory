package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.MapeadorCatalogos;
import com.art.inventario.persistencia.consulta.ParametroLegalConsultaJpa;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;

@Repository
@Transactional(readOnly = true)
public class ParametroLegalPersistenciaJpa implements ParametroLegalPersistencia {

	private final ParametroLegalConsultaJpa consulta;

	public ParametroLegalPersistenciaJpa(ParametroLegalConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<ParametroLegal> listar() {
		return MapeadorCatalogos.aDominioParametros(consulta.findAll());
	}

	@Override
	public ParametroLegal obtener(Long id) {
		return consulta.findById(id)
				.map(MapeadorCatalogos::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Parámetro legal no encontrado"));
	}

	@Override
	@Transactional
	public ParametroLegal guardar(ParametroLegal parametro) {
		return MapeadorCatalogos.aDominio(consulta.save(MapeadorCatalogos.aEntidad(parametro)));
	}

	@Override
	public Optional<ParametroLegal> porAnio(int anio) {
		return consulta.findByAnio(anio).map(MapeadorCatalogos::aDominio);
	}

	@Override
	public boolean existeAnio(int anio) {
		return consulta.existsByAnio(anio);
	}
}
