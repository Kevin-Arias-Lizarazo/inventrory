package com.art.inventario.aplicacion;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.EppCasoDeUso;
import com.art.inventario.puerto.salida.AjusteConsultaSalida;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.DevolucionPersistencia;
import com.art.inventario.puerto.salida.EntregaEppPersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;

@Service
public class EppAplicacion implements EppCasoDeUso {

	private final EppPersistencia persistencia;
	private final EntregaEppPersistencia entregaPersistencia;
	private final CambiosNotificador notificador;
	private final AjusteConsultaSalida ajustes;
	private final DevolucionPersistencia devoluciones;

	public EppAplicacion(EppPersistencia persistencia, EntregaEppPersistencia entregaPersistencia,
			CambiosNotificador notificador, AjusteConsultaSalida ajustes, DevolucionPersistencia devoluciones) {
		this.persistencia = persistencia;
		this.entregaPersistencia = entregaPersistencia;
		this.notificador = notificador;
		this.ajustes = ajustes;
		this.devoluciones = devoluciones;
	}

	@Override
	public List<Epp> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Epp> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public PaginaResultado<Epp> listarPagina(ConsultaPaginada consulta) {
		return persistencia.listarPagina(consulta);
	}

	@Override
	public Epp obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Epp crear(Epp epp) {
		validarNombre(epp);
		validarNombreUnico(epp.getNombre(), null);
		if (epp.getStock() == null) {
			epp.setStock(0);
		}
		Epp creado = persistencia.guardar(epp);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		return creado;
	}

	@Override
	@Transactional
	public Epp actualizar(Long id, Epp datos) {
		Epp actual = persistencia.obtener(id);
		validarNombre(datos);
		validarNombreUnico(datos.getNombre(), id);
		actual.setNombre(datos.getNombre());
		actual.setMarca(datos.getMarca());
		actual.setStockMinimo(datos.getStockMinimo());
		actual.setFechaVencimiento(datos.getFechaVencimiento());
		actual.setDescripcion(datos.getDescripcion());
		actual.setFotoUrl(datos.getFotoUrl());
		Epp guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (entregaPersistencia.tieneEntregasConEpp(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el EPP tiene entregas asociadas");
		}
		if (persistencia.tieneMovimientos(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el EPP tiene movimientos asociados");
		}
		if (ajustes.tieneProducto("EPP", id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el EPP tiene ajustes asociados");
		}
		if (devoluciones.tieneProducto("EPP", id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el EPP tiene devoluciones asociadas");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
	}

	@Override
	public List<MovimientoEpp> listarMovimientos(Long eppId) {
		persistencia.obtener(eppId);
		return persistencia.listarMovimientos(eppId);
	}

	@Override
	public List<MovimientoEpp> listarTodosMovimientos() {
		return persistencia.listarTodosMovimientos().stream()
				.sorted(Comparator.comparing(EppAplicacion::fechaComparable).reversed())
				.toList();
	}

	@Override
	public PaginaResultado<MovimientoEpp> listarTodosMovimientosPagina(ConsultaPaginada consulta) {
		return persistencia.listarTodosMovimientosPagina(consulta);
	}

	@Override
	public PaginaResultado<MovimientoEpp> listarMovimientosPagina(Long eppId, ConsultaPaginada consulta) {
		persistencia.obtener(eppId);
		return persistencia.listarMovimientosPagina(eppId, consulta);
	}

	@Override
	@Transactional
	public MovimientoEpp registrarMovimiento(Long eppId, MovimientoEpp movimiento) {
		validarMovimiento(movimiento);
		Epp epp = persistencia.obtener(eppId);
		int signo = signo(movimiento.getTipo());
		int nuevoStock = stock(epp) + signo * movimiento.getCantidad();
		if (nuevoStock < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		epp.setStock(nuevoStock);
		persistencia.guardar(epp);
		movimiento.setEpp(epp);
		MovimientoEpp creado = persistencia.guardarMovimiento(movimiento);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_EPP);
		return creado;
	}

	@Override
	@Transactional
	public MovimientoEpp actualizarMovimiento(Long id, MovimientoEpp datos) {
		validarMovimiento(datos);
		MovimientoEpp actual = persistencia.obtenerMovimiento(id);
		Epp epp = actual.getEpp();
		int signoNuevo = signo(datos.getTipo());
		int ajuste = signoNuevo * datos.getCantidad() - signo(actual.getTipo()) * actual.getCantidad();
		int nuevoStock = stock(epp) + ajuste;
		if (nuevoStock < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		epp.setStock(nuevoStock);
		persistencia.guardar(epp);
		actual.setTipo(datos.getTipo());
		actual.setCantidad(datos.getCantidad());
		actual.setFecha(datos.getFecha());
		actual.setObservacion(datos.getObservacion());
		MovimientoEpp guardado = persistencia.guardarMovimiento(actual);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_EPP);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminarMovimiento(Long id) {
		MovimientoEpp actual = persistencia.obtenerMovimiento(id);
		Epp epp = actual.getEpp();
		int nuevoStock = stock(epp) - signo(actual.getTipo()) * actual.getCantidad();
		if (nuevoStock < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para realizar el egreso");
		}
		epp.setStock(nuevoStock);
		persistencia.guardar(epp);
		persistencia.eliminarMovimiento(actual);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		notificador.publicar(CambiosNotificador.RECURSO_MOVIMIENTOS_EPP);
	}

	private void validarMovimiento(MovimientoEpp movimiento) {
		if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
			throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
		}
		if (!"INGRESO".equals(movimiento.getTipo()) && !"EGRESO".equals(movimiento.getTipo())) {
			throw new DatosInvalidosExcepcion("El tipo debe ser INGRESO o EGRESO");
		}
	}

	private static int stock(Epp epp) {
		return epp.getStock() == null ? 0 : epp.getStock();
	}

	private static int signo(String tipo) {
		return "INGRESO".equals(tipo) ? 1 : -1;
	}

	private static String fechaComparable(MovimientoEpp m) {
		return String.valueOf(m.getFecha() == null ? "" : m.getFecha())
				.replace("-", "")
				.replace("/", "");
	}

	private void validarNombre(Epp epp) {
		if (epp.getNombre() == null || epp.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe un EPP con ese nombre");
		}
	}
}