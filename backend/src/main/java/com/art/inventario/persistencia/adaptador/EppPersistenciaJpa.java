package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.entidad.EntidadEpp;
import com.art.inventario.persistencia.consulta.EppConsultaJpa;
import com.art.inventario.persistencia.consulta.MovimientoEppConsultaJpa;
import com.art.inventario.puerto.salida.EppPersistencia;

@Repository
@Transactional(readOnly = true)
public class EppPersistenciaJpa implements EppPersistencia {

	private final EppConsultaJpa consulta;
	private final MovimientoEppConsultaJpa movimientosConsulta;

	public EppPersistenciaJpa(EppConsultaJpa consulta, MovimientoEppConsultaJpa movimientosConsulta) {
		this.consulta = consulta;
		this.movimientosConsulta = movimientosConsulta;
	}

	@Override
	public List<Epp> listar() {
		return Mapeador.aDominioEpps(consulta.findAll());
	}

	@Override
	public PaginaResultado<Epp> listarPagina(int pagina, int tamano) {
		Page<EntidadEpp> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Epp> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Epp obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("EPP no encontrado"));
	}

	@Override
	@Transactional
	public Epp guardar(Epp epp) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(epp)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("EPP no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneMovimientos(Long id) {
		return movimientosConsulta.existsByEppId(id);
	}

	@Override
	public List<MovimientoEpp> listarMovimientos(Long eppId) {
		return Mapeador.aDominioMovimientosEpp(movimientosConsulta.findByEppIdOrderByFechaDesc(eppId));
	}

	@Override
	public List<MovimientoEpp> listarTodosMovimientos() {
		return Mapeador.aDominioMovimientosEpp(movimientosConsulta.findAll());
	}

	@Override
	public MovimientoEpp obtenerMovimiento(Long id) {
		return movimientosConsulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Movimiento no encontrado"));
	}

	@Override
	@Transactional
	public MovimientoEpp guardarMovimiento(MovimientoEpp movimiento) {
		EntidadEpp epp = resolverEpp(movimiento.getEpp() == null ? null : movimiento.getEpp().getId());
		return Mapeador.aDominio(movimientosConsulta.save(Mapeador.aEntidad(movimiento, epp)));
	}

	@Override
	@Transactional
	public void eliminarMovimiento(MovimientoEpp movimiento) {
		movimientosConsulta.deleteById(movimiento.getId());
	}

	private EntidadEpp resolverEpp(Long eppId) {
		if (eppId == null) {
			throw new NoEncontradoExcepcion("EPP no encontrado");
		}
		return consulta.findById(eppId)
				.orElseThrow(() -> new NoEncontradoExcepcion("EPP no encontrado"));
	}
}