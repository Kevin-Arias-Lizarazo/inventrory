package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.HerramientaConsultaJpa;
import com.art.inventario.persistencia.consulta.MovimientoHerramientaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.puerto.salida.MovimientoHerramientaPersistencia;

@Repository
@Transactional(readOnly = true)
public class MovimientoHerramientaPersistenciaJpa implements MovimientoHerramientaPersistencia {

	private final MovimientoHerramientaConsultaJpa consulta;
	private final HerramientaConsultaJpa herramientaConsulta;

	public MovimientoHerramientaPersistenciaJpa(MovimientoHerramientaConsultaJpa consulta,
			HerramientaConsultaJpa herramientaConsulta) {
		this.consulta = consulta;
		this.herramientaConsulta = herramientaConsulta;
	}

	@Override
	public List<MovimientoHerramienta> listarPorHerramienta(Long herramientaId) {
		return Mapeador.aDominioMovimientosHerramienta(consulta.findByHerramientaIdOrderByFechaDesc(herramientaId));
	}

	@Override
	public List<MovimientoHerramienta> listarTodos() {
		return Mapeador.aDominioMovimientosHerramienta(consulta.findAll());
	}

	@Override
	public MovimientoHerramienta obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Movimiento no encontrado"));
	}

	@Override
	@Transactional
	public MovimientoHerramienta guardar(MovimientoHerramienta movimiento) {
		EntidadHerramienta herramienta = resolverHerramienta(
				movimiento.getHerramienta() == null ? null : movimiento.getHerramienta().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(movimiento, herramienta)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Movimiento no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	@Transactional
	public void eliminarPorHerramienta(Long herramientaId) {
		consulta.deleteByHerramientaId(herramientaId);
	}

	private EntidadHerramienta resolverHerramienta(Long herramientaId) {
		if (herramientaId == null) {
			return null;
		}
		return herramientaConsulta.findById(herramientaId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Herramienta no encontrada"));
	}
}