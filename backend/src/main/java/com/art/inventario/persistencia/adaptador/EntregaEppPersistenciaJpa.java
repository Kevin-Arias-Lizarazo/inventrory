package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.entidad.EntidadEntregaEpp;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.EntregaEppConsultaJpa;
import com.art.inventario.persistencia.consulta.EppConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadEpp;
import com.art.inventario.puerto.salida.EntregaEppPersistencia;

@Repository
@Transactional(readOnly = true)
public class EntregaEppPersistenciaJpa implements EntregaEppPersistencia {

	private final EntregaEppConsultaJpa consulta;
	private final EmpleadoConsultaJpa empleadoConsulta;
	private final EppConsultaJpa eppConsulta;

	public EntregaEppPersistenciaJpa(EntregaEppConsultaJpa consulta, EmpleadoConsultaJpa empleadoConsulta,
			EppConsultaJpa eppConsulta) {
		this.consulta = consulta;
		this.empleadoConsulta = empleadoConsulta;
		this.eppConsulta = eppConsulta;
	}

	@Override
	public List<EntregaEpp> listar() {
		return Mapeador.aDominioEntregasEpp(consulta.findAll());
	}

	@Override
	public PaginaResultado<EntregaEpp> listarPagina(int pagina, int tamano) {
		Page<EntidadEntregaEpp> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<EntregaEpp> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public EntregaEpp obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Entrega no encontrada"));
	}

	@Override
	@Transactional
	public EntregaEpp guardar(EntregaEpp entrega) {
		EntidadEmpleado empleado = resolverEmpleado(
				entrega.getEmpleado() == null ? null : entrega.getEmpleado().getId());
		EntidadEpp epp = resolverEpp(entrega.getEpp() == null ? null : entrega.getEpp().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(entrega, empleado, epp)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Entrega no encontrada");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneEntregasConEpp(Long eppId) {
		return consulta.existsByEppId(eppId);
	}

	private EntidadEmpleado resolverEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			return null;
		}
		return empleadoConsulta.findById(empleadoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}

	private EntidadEpp resolverEpp(Long eppId) {
		if (eppId == null) {
			return null;
		}
		return eppConsulta.findById(eppId)
				.orElseThrow(() -> new NoEncontradoExcepcion("EPP no encontrado"));
	}
}