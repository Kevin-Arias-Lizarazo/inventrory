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
import com.art.inventario.dominio.Proveedor;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.CompraConsultaJpa;
import com.art.inventario.persistencia.consulta.Especificaciones;
import com.art.inventario.persistencia.consulta.Especificaciones.CampoFiltro;
import com.art.inventario.persistencia.consulta.Especificaciones.TipoFiltro;
import com.art.inventario.persistencia.consulta.FacturaConsultaJpa;
import com.art.inventario.persistencia.consulta.ProveedorConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadProveedor;
import com.art.inventario.puerto.salida.ProveedorPersistencia;

@Repository
@Transactional(readOnly = true)
public class ProveedorPersistenciaJpa implements ProveedorPersistencia {

	private static final Map<String, CampoFiltro> CAMPOS = Map.of();

	private static final List<String> BUSCABLES = List.of("nombre", "correo", "telefono", "direccion");

	private static final Set<String> ORDENABLES = Set.of("id", "nombre");

	private final ProveedorConsultaJpa consulta;
	private final CompraConsultaJpa compraConsulta;
	private final FacturaConsultaJpa facturaConsulta;

	public ProveedorPersistenciaJpa(ProveedorConsultaJpa consulta, CompraConsultaJpa compraConsulta,
			FacturaConsultaJpa facturaConsulta) {
		this.consulta = consulta;
		this.compraConsulta = compraConsulta;
		this.facturaConsulta = facturaConsulta;
	}

	@Override
	public List<Proveedor> listar() {
		return Mapeador.aDominioProveedores(consulta.findAll());
	}

	@Override
	public PaginaResultado<Proveedor> listarPagina(int pagina, int tamano) {
		Page<EntidadProveedor> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Proveedor> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public PaginaResultado<Proveedor> listarPagina(ConsultaPaginada consultaPaginada) {
		Specification<EntidadProveedor> spec = Especificaciones.<EntidadProveedor>filtrar(
				consultaPaginada, CAMPOS, BUSCABLES);
		Sort sort = Especificaciones.ordenar(consultaPaginada, ORDENABLES, "id");
		Page<EntidadProveedor> page = consulta.findAll(spec,
				PageRequest.of(consultaPaginada.getPagina(), consultaPaginada.getTamano(), sort));
		List<Proveedor> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, consultaPaginada.getPagina(), consultaPaginada.getTamano(),
				page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Proveedor obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Proveedor no encontrado"));
	}

	@Override
	@Transactional
	public Proveedor guardar(Proveedor proveedor) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(proveedor)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Proveedor no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneComprasOFacturas(Long id) {
		return compraConsulta.existsByProveedorId(id) || facturaConsulta.existsByProveedorId(id);
	}
}