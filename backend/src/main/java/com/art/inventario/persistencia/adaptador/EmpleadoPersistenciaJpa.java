package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Empleado;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.AsignacionHerramientaConsultaJpa;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.EntregaEppConsultaJpa;
import com.art.inventario.persistencia.consulta.EntregaRopaConsultaJpa;
import com.art.inventario.persistencia.consulta.MinutaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;

@Repository
@Transactional(readOnly = true)
public class EmpleadoPersistenciaJpa implements EmpleadoPersistencia {

	private final EmpleadoConsultaJpa consulta;
	private final MinutaConsultaJpa minutas;
	private final EntregaRopaConsultaJpa entregasRopa;
	private final EntregaEppConsultaJpa entregasEpp;
	private final AsignacionHerramientaConsultaJpa asignaciones;

	public EmpleadoPersistenciaJpa(EmpleadoConsultaJpa consulta, MinutaConsultaJpa minutas,
			EntregaRopaConsultaJpa entregasRopa, EntregaEppConsultaJpa entregasEpp,
			AsignacionHerramientaConsultaJpa asignaciones) {
		this.consulta = consulta;
		this.minutas = minutas;
		this.entregasRopa = entregasRopa;
		this.entregasEpp = entregasEpp;
		this.asignaciones = asignaciones;
	}

	@Override
	public List<Empleado> todos() {
		return consulta.findAll().stream().map(Mapeador::aDominio).toList();
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Empleado obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}

	@Override
	public Empleado obtenerPorCodigo(String codigo) {
		EntidadEmpleado entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Empleado no encontrado");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	public boolean existePorCodigo(String codigo) {
		return consulta.findByCodigo(codigo) != null;
	}

	@Override
	@Transactional
	public Empleado guardar(Empleado empleado) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(empleado)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Empleado no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneReferencias(Long id) {
		return minutas.existsByEmpleadoId(id)
				|| entregasRopa.existsByEmpleadoId(id)
				|| entregasEpp.existsByEmpleadoId(id)
				|| asignaciones.existsByEmpleadoId(id);
	}
}