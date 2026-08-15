package com.art.inventario.aplicacion;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.MaterialCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.MaterialPersistencia;

@Service
public class MaterialAplicacion implements MaterialCasoDeUso {

	private final MaterialPersistencia persistencia;
	private final CambiosNotificador notificador;

	public MaterialAplicacion(MaterialPersistencia persistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Material> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Material> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public Material obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Material crear(Material material) {
		validarNombre(material);
		validarNombreUnico(material.getNombre(), null);
		if (material.getStock() == null) {
			material.setStock(0);
		}
		Material creado = persistencia.guardar(material);
		notificador.publicar(CambiosNotificador.RECURSO_MATERIALES);
		return creado;
	}

	@Override
	@Transactional
	public Material actualizar(Long id, Material datos) {
		Material actual = persistencia.obtener(id);
		validarNombre(datos);
		validarNombreUnico(datos.getNombre(), id);
		actual.setNombre(datos.getNombre());
		actual.setUnidad(datos.getUnidad());
		actual.setDescripcion(datos.getDescripcion());
		actual.setFotoUrl(datos.getFotoUrl());
		Material guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_MATERIALES);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (persistencia.tieneMovimientos(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el material tiene movimientos asociados");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_MATERIALES);
	}

	@Override
	public List<MovimientoMaterial> listarMovimientos(Long materialId) {
		persistencia.obtener(materialId);
		return persistencia.listarMovimientos(materialId);
	}

	@Override
	public List<MovimientoMaterial> listarTodosMovimientos() {
		return persistencia.listarTodosMovimientos().stream()
				.sorted(Comparator.comparing(MaterialAplicacion::fechaComparable).reversed())
				.toList();
	}

	@Override
	@Transactional
	public MovimientoMaterial registrarMovimiento(Long materialId, MovimientoMaterial movimiento) {
		validarMovimiento(movimiento);
		Material material = persistencia.obtener(materialId);
		int signo = signo(movimiento.getTipo());
		int nuevoStock = stock(material) + signo * movimiento.getCantidad();
		if (nuevoStock < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		material.setStock(nuevoStock);
		persistencia.guardar(material);
		movimiento.setMaterial(material);
		MovimientoMaterial creado = persistencia.guardarMovimiento(movimiento);
		notificador.publicar(CambiosNotificador.RECURSO_MATERIALES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_MATERIALES);
		return creado;
	}

	@Override
	@Transactional
	public MovimientoMaterial actualizarMovimiento(Long id, MovimientoMaterial datos) {
		validarMovimiento(datos);
		MovimientoMaterial actual = persistencia.obtenerMovimiento(id);
		Material material = actual.getMaterial();
		int signoNuevo = signo(datos.getTipo());
		int ajuste = signoNuevo * datos.getCantidad() - signo(actual.getTipo()) * actual.getCantidad();
		int nuevoStock = stock(material) + ajuste;
		if (nuevoStock < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		material.setStock(nuevoStock);
		persistencia.guardar(material);
		actual.setTipo(datos.getTipo());
		actual.setCantidad(datos.getCantidad());
		actual.setFecha(datos.getFecha());
		actual.setObservacion(datos.getObservacion());
		MovimientoMaterial guardado = persistencia.guardarMovimiento(actual);
		notificador.publicar(CambiosNotificador.RECURSO_MATERIALES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_MATERIALES);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminarMovimiento(Long id) {
		MovimientoMaterial actual = persistencia.obtenerMovimiento(id);
		Material material = actual.getMaterial();
		material.setStock(stock(material) - signo(actual.getTipo()) * actual.getCantidad());
		persistencia.guardar(material);
		persistencia.eliminarMovimiento(actual);
		notificador.publicar(CambiosNotificador.RECURSO_MATERIALES);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_MATERIALES);
	}

	private void validarMovimiento(MovimientoMaterial movimiento) {
		if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
			throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
		}
		if (!"INGRESO".equals(movimiento.getTipo()) && !"EGRESO".equals(movimiento.getTipo())) {
			throw new DatosInvalidosExcepcion("El tipo debe ser INGRESO o EGRESO");
		}
	}

	private void validarNombre(Material material) {
		if (material.getNombre() == null || material.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe un material con ese nombre");
		}
	}

	private static int stock(Material material) {
		return material.getStock() == null ? 0 : material.getStock();
	}

	private static int signo(String tipo) {
		return "INGRESO".equals(tipo) ? 1 : -1;
	}

	private static String fechaComparable(MovimientoMaterial m) {
		return String.valueOf(m.getFecha() == null ? "" : m.getFecha())
				.replace("-", "")
				.replace("/", "");
	}
}