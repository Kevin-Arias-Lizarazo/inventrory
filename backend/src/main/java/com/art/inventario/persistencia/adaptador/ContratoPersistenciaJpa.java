package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Contrato;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.ContratoConsultaJpa;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.TipoContratoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadTipoContrato;
import com.art.inventario.puerto.salida.ContratoPersistencia;

@Repository
@Transactional(readOnly = true)
public class ContratoPersistenciaJpa implements ContratoPersistencia {

	private final ContratoConsultaJpa consulta;
	private final EmpleadoConsultaJpa empleadoConsulta;
	private final TipoContratoConsultaJpa tipoContratoConsulta;

	public ContratoPersistenciaJpa(ContratoConsultaJpa consulta, EmpleadoConsultaJpa empleadoConsulta,
			TipoContratoConsultaJpa tipoContratoConsulta) {
		this.consulta = consulta;
		this.empleadoConsulta = empleadoConsulta;
		this.tipoContratoConsulta = tipoContratoConsulta;
	}

	@Override
	public List<Contrato> listar() {
		return Mapeador.aDominioContratos(consulta.findAll());
	}

	@Override
	public Contrato obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Contrato no encontrado"));
	}

	@Override
	@Transactional
	public Contrato guardar(Contrato contrato) {
		EntidadEmpleado empleado = resolverEmpleado(
				contrato.getEmpleado() == null ? null : contrato.getEmpleado().getId());
		EntidadTipoContrato tipo = resolverTipoContrato(
				contrato.getTipoContrato() == null ? null : contrato.getTipoContrato().getId());
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(contrato, empleado, tipo)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Contrato no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean empleadoContratado(Long empleadoId) {
		return consulta.existsByEmpleadoIdAndEstado(empleadoId, Contrato.ACTIVO);
	}

	@Override
	public List<Long> empleadosContratados() {
		return consulta.empleadosConEstado(Contrato.ACTIVO);
	}

	@Override
	public boolean tieneContratos(Long empleadoId) {
		return consulta.existsByEmpleadoId(empleadoId);
	}

	private EntidadEmpleado resolverEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			return null;
		}
		return empleadoConsulta.findById(empleadoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Empleado no encontrado"));
	}

	private EntidadTipoContrato resolverTipoContrato(Long tipoContratoId) {
		if (tipoContratoId == null) {
			return null;
		}
		return tipoContratoConsulta.findById(tipoContratoId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Tipo de contrato no encontrado"));
	}
}