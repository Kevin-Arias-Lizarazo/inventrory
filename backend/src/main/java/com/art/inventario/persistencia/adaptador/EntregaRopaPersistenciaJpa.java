package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaRopa;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.entidad.EntidadEntregaRopa;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.EntregaRopaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.puerto.salida.EntregaRopaPersistencia;

@Repository
@Transactional(readOnly = true)
public class EntregaRopaPersistenciaJpa implements EntregaRopaPersistencia {

	private final EntregaRopaConsultaJpa consulta;
	private final EmpleadoConsultaJpa empleadoConsulta;

	public EntregaRopaPersistenciaJpa(EntregaRopaConsultaJpa consulta, EmpleadoConsultaJpa empleadoConsulta) {
		this.consulta = consulta;
		this.empleadoConsulta = empleadoConsulta;
	}

	@Override
	public List<EntregaRopa> listar() {
		return Mapeador.aDominioEntregasRopa(consulta.findAll());
	}

	@Override
	public PaginaResultado<EntregaRopa> listarPagina(int pagina, int tamano) {
		Page<EntidadEntregaRopa> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<EntregaRopa> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public EntregaRopa obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Entrega no encontrada"));
	}

	@Override
	@Transactional
	public EntregaRopa guardar(EntregaRopa entrega) {
		EntidadEmpleado empleado = resolverEmpleado(entrega.getEmpleado() == null ? null : entrega.getEmpleado().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(entrega, empleado)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Entrega no encontrada");
		}
		consulta.deleteById(id);
	}

	private EntidadEmpleado resolverEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			return null;
		}
		return empleadoConsulta.findById(empleadoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}
}