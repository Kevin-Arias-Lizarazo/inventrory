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
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.MaterialConsultaJpa;
import com.art.inventario.persistencia.consulta.MovimientoMaterialConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadMaterial;
import com.art.inventario.persistencia.entidad.EntidadMovimientoMaterial;
import com.art.inventario.puerto.salida.MaterialPersistencia;

@Repository
@Transactional(readOnly = true)
public class MaterialPersistenciaJpa implements MaterialPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"marca", new CampoFiltro("marca", TipoFiltro.TEXTO_EXACTO),
			"unidad", new CampoFiltro("unidad", TipoFiltro.TEXTO_EXACTO));

	private static final List<String> BUSCABLES = List.of("nombre", "marca", "descripcion");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "nombre", "marca", "stock", "ultimoCosto", "stockMinimo");

	private static final Map<String, CampoFiltro> CAMPOS_MOVIMIENTOS = Map.of(
			"recursoId", new CampoFiltro("material.id", TipoFiltro.ID),
			"tipo", new CampoFiltro("tipo", TipoFiltro.TEXTO_EXACTO),
			"fechaDesde", new CampoFiltro(
					(r, q, cb, v) -> cb.greaterThanOrEqualTo(r.get("fecha").as(String.class), v), TipoFiltro.FECHA),
			"fechaHasta", new CampoFiltro(
					(r, q, cb, v) -> cb.lessThanOrEqualTo(r.get("fecha").as(String.class), v), TipoFiltro.FECHA));

	private static final List<String> BUSCABLES_MOVIMIENTOS = List.of("observacion");

	private static final Set<String> ORDENABLES_MOVIMIENTOS = Set.of("id", "fecha", "tipo", "cantidad");

	private final MaterialConsultaJpa consulta;
	private final MovimientoMaterialConsultaJpa movimientosConsulta;

	public MaterialPersistenciaJpa(MaterialConsultaJpa consulta, MovimientoMaterialConsultaJpa movimientosConsulta) {
		this.consulta = consulta;
		this.movimientosConsulta = movimientosConsulta;
	}

	@Override
	public List<Material> listar() {
		return Mapeador.aDominioMateriales(consulta.findAll());
	}

	@Override
	public PaginaResultado<Material> listarPagina(int pagina, int tamano) {
		Page<EntidadMaterial> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Material> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<Material> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadMaterial> spec = Especificaciones.<EntidadMaterial>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadMaterial> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Material> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Material obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Material no encontrado"));
	}

	@Override
	@Transactional
	public Material guardar(Material material) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(material)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Material no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneMovimientos(Long id) {
		return movimientosConsulta.existsByMaterialId(id);
	}

	@Override
	public List<MovimientoMaterial> listarMovimientos(Long materialId) {
		return Mapeador.aDominioMovimientosMaterial(movimientosConsulta.findByMaterialIdOrderByFechaDesc(materialId));
	}

	@Override
	public List<MovimientoMaterial> listarTodosMovimientos() {
		return Mapeador.aDominioMovimientosMaterial(movimientosConsulta.findAll());
	}

	@Override
	public PaginaResultado<MovimientoMaterial> listarTodosMovimientosPagina(ConsultaPaginada c) {
		Especificaciones.validarRangoFechas(c);
		Specification<EntidadMovimientoMaterial> spec = Especificaciones.<EntidadMovimientoMaterial>filtrar(
				c, CAMPOS_MOVIMIENTOS, BUSCABLES_MOVIMIENTOS);
		Sort sort = Especificaciones.ordenarMovimientos(c, ORDENABLES_MOVIMIENTOS);
		Page<EntidadMovimientoMaterial> page = movimientosConsulta.findAll(spec,
				PageRequest.of(c.getPagina(), c.getTamano(), sort));
		return new PaginaResultado<>(Mapeador.aDominioMovimientosMaterial(page.getContent()),
				c.getPagina(), c.getTamano(), page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<MovimientoMaterial> listarMovimientosPagina(Long materialId, ConsultaPaginada c) {
		ConsultaPaginada conRecurso = c.conCopy();
		conRecurso.getFiltros().put("recursoId", String.valueOf(materialId));
		return listarTodosMovimientosPagina(conRecurso);
	}

	@Override
	public MovimientoMaterial obtenerMovimiento(Long id) {
		return movimientosConsulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Movimiento no encontrado"));
	}

	@Override
	@Transactional
	public MovimientoMaterial guardarMovimiento(MovimientoMaterial movimiento) {
		EntidadMaterial material = resolverMaterial(movimiento.getMaterial() == null ? null : movimiento.getMaterial().getId());
		return Mapeador.aDominio(movimientosConsulta.save(Mapeador.aEntidad(movimiento, material)));
	}

	@Override
	@Transactional
	public void eliminarMovimiento(MovimientoMaterial movimiento) {
		movimientosConsulta.deleteById(movimiento.getId());
	}

	private EntidadMaterial resolverMaterial(Long materialId) {
		if (materialId == null) {
			throw new NoEncontradoExcepcion("Material no encontrado");
		}
		return consulta.findById(materialId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Material no encontrado"));
	}
}