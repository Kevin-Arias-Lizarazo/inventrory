package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Proyecto;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.ProyectoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadProyecto;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Repository
@Transactional(readOnly = true)
public class ProyectoPersistenciaJpa implements ProyectoPersistencia {

	private final ProyectoConsultaJpa consulta;

	public ProyectoPersistenciaJpa(ProyectoConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Proyecto> listar() {
		return Mapeador.aDominioProyectos(consulta.findAll());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Proyecto obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Proyecto no encontrado"));
	}

	@Override
	public Proyecto obtenerPorCodigo(String codigo) {
		EntidadProyecto entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Proyecto no encontrado");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	@Transactional
	public Proyecto guardar(Proyecto proyecto) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(proyecto)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Proyecto no encontrado");
		}
		consulta.deleteById(id);
	}
}