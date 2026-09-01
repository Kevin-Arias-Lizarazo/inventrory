package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.ContratoConsultaJpa;
import com.art.inventario.persistencia.consulta.EmpleadoConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.TipoContratoConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadContrato;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadTipoContrato;
import com.art.inventario.puerto.salida.ContratoPersistencia;

@Repository
@Transactional(readOnly = true)
public class ContratoPersistenciaJpa implements ContratoPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"estado", new CampoFiltro("estado", TipoFiltro.TEXTO_EXACTO),
			"empleadoId", new CampoFiltro("empleado.id", TipoFiltro.ID),
			"tipoContratoId", new CampoFiltro("tipoContrato.id", TipoFiltro.ID));

	private static final List<String> BUSCABLES = List.of("empleado.nombre");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "fechaInicio", "fechaFin", "estado", "remuneracionMensual", "empleado.nombre");

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
	public PaginaResultado<Contrato> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadContrato> spec = Especificaciones.<EntidadContrato>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadContrato> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Contrato> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
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