package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.entidad.EntidadAsignacionConsumible;
import com.art.inventario.persistencia.consulta.AsignacionConsumibleConsultaJpa;
import com.art.inventario.persistencia.consulta.ConsumibleConsultaJpa;
import com.art.inventario.persistencia.consulta.ProyectoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadConsumible;
import com.art.inventario.persistencia.entidad.EntidadProyecto;
import com.art.inventario.puerto.salida.AsignacionConsumiblePersistencia;

@Repository
@Transactional(readOnly = true)
public class AsignacionConsumiblePersistenciaJpa implements AsignacionConsumiblePersistencia {

	private final AsignacionConsumibleConsultaJpa consulta;
	private final ConsumibleConsultaJpa consumibleConsulta;
	private final ProyectoConsultaJpa proyectoConsulta;

	public AsignacionConsumiblePersistenciaJpa(AsignacionConsumibleConsultaJpa consulta,
			ConsumibleConsultaJpa consumibleConsulta, ProyectoConsultaJpa proyectoConsulta) {
		this.consulta = consulta;
		this.consumibleConsulta = consumibleConsulta;
		this.proyectoConsulta = proyectoConsulta;
	}

	@Override
	public List<AsignacionConsumible> listar() {
		return Mapeador.aDominioAsignacionesConsumibles(consulta.findAll());
	}

	@Override
	public PaginaResultado<AsignacionConsumible> listarPagina(int pagina, int tamano) {
		Page<EntidadAsignacionConsumible> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<AsignacionConsumible> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public AsignacionConsumible obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Asignación no encontrada"));
	}

	@Override
	@Transactional
	public AsignacionConsumible guardar(AsignacionConsumible asignacion) {
		EntidadConsumible consumible = resolverConsumible(
				asignacion.getConsumible() == null ? null : asignacion.getConsumible().getId());
		EntidadProyecto proyecto = resolverProyecto(
				asignacion.getProyecto() == null ? null : asignacion.getProyecto().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(asignacion, consumible, proyecto)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Asignación no encontrada");
		}
		consulta.deleteById(id);
	}

	private EntidadConsumible resolverConsumible(Long consumibleId) {
		if (consumibleId == null) {
			return null;
		}
		return consumibleConsulta.findById(consumibleId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Consumible no encontrado"));
	}

	private EntidadProyecto resolverProyecto(Long proyectoId) {
		if (proyectoId == null) {
			return null;
		}
		return proyectoConsulta.findById(proyectoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Proyecto no encontrado"));
	}
}