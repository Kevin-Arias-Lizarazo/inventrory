package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.NivelAcceso;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.NivelAccesoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadNivelAcceso;
import com.art.inventario.puerto.salida.NivelAccesoPersistencia;

@Repository
@Transactional(readOnly = true)
public class NivelAccesoPersistenciaJpa implements NivelAccesoPersistencia {

	private final NivelAccesoConsultaJpa consulta;

	public NivelAccesoPersistenciaJpa(NivelAccesoConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public Optional<NivelAcceso> porCodigo(String codigo) {
		return consulta.findByCodigo(codigo).map(Mapeador::aDominio);
	}

	@Override
	public Optional<Long> usuarioRaizId() {
		return consulta.findByUsuarioRaizIdIsNotNull().map(EntidadNivelAcceso::getUsuarioRaizId);
	}

	@Override
	@Transactional
	public void guardar(NivelAcceso nivel) {
		consulta.save(Mapeador.aEntidad(nivel));
	}

	@Override
	public List<NivelAcceso> todos() {
		return consulta.findAll().stream().map(Mapeador::aDominio).toList();
	}
}