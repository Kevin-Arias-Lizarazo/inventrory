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
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.ConsumibleConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.MovimientoConsumibleConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadConsumible;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;

@Repository
@Transactional(readOnly = true)
public class ConsumiblePersistenciaJpa implements ConsumiblePersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of(
			"marca", new CampoFiltro("marca", TipoFiltro.TEXTO_EXACTO),
			"unidad", new CampoFiltro("unidad", TipoFiltro.TEXTO_EXACTO));

	private static final List<String> BUSCABLES = List.of("codigo", "nombre", "marca", "descripcion");

	private static final Set<String> ORDENABLES = Set.of(
			"id", "codigo", "nombre", "marca", "stock", "ultimoCosto", "stockMinimo");

	private final ConsumibleConsultaJpa consulta;
	private final MovimientoConsumibleConsultaJpa movimientosConsulta;

	public ConsumiblePersistenciaJpa(ConsumibleConsultaJpa consulta,
			MovimientoConsumibleConsultaJpa movimientosConsulta) {
		this.consulta = consulta;
		this.movimientosConsulta = movimientosConsulta;
	}

	@Override
	public List<Consumible> listar() {
		return Mapeador.aDominioConsumibles(consulta.findAll());
	}

	@Override
	public PaginaResultado<Consumible> listarPagina(int pagina, int tamano) {
		Page<EntidadConsumible> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Consumible> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<Consumible> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadConsumible> spec = Especificaciones.<EntidadConsumible>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadConsumible> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Consumible> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Consumible obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Consumible no encontrado"));
	}

	@Override
	public Consumible obtenerPorCodigo(String codigo) {
		EntidadConsumible entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Consumible no encontrado");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	public boolean existePorCodigo(String codigo) {
		return consulta.findByCodigo(codigo) != null;
	}

	@Override
	@Transactional
	public Consumible guardar(Consumible consumible) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(consumible)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Consumible no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneMovimientos(Long id) {
		return movimientosConsulta.existsByConsumibleId(id);
	}

	@Override
	public List<MovimientoConsumible> listarMovimientos(Long consumibleId) {
		return Mapeador.aDominioMovimientosConsumible(
				movimientosConsulta.findByConsumibleIdOrderByFechaDesc(consumibleId));
	}

	@Override
	public List<MovimientoConsumible> listarTodosMovimientos() {
		return Mapeador.aDominioMovimientosConsumible(movimientosConsulta.findAll());
	}

	@Override
	public MovimientoConsumible obtenerMovimiento(Long id) {
		return movimientosConsulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Movimiento no encontrado"));
	}

	@Override
	@Transactional
	public MovimientoConsumible guardarMovimiento(MovimientoConsumible movimiento) {
		EntidadConsumible consumible = resolverConsumible(
				movimiento.getConsumible() == null ? null : movimiento.getConsumible().getId());
		return Mapeador.aDominio(movimientosConsulta.save(Mapeador.aEntidad(movimiento, consumible)));
	}

	@Override
	@Transactional
	public void eliminarMovimiento(MovimientoConsumible movimiento) {
		movimientosConsulta.deleteById(movimiento.getId());
	}

	private EntidadConsumible resolverConsumible(Long consumibleId) {
		if (consumibleId == null) {
			throw new NoEncontradoExcepcion("Consumible no encontrado");
		}
		return consulta.findById(consumibleId)
				.orElseThrow(() -> new NoEncontradoExcepcion("Consumible no encontrado"));
	}
}