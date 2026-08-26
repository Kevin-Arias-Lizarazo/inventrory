package com.art.inventario.aplicacion;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.AsignacionConsumibleCasoDeUso;
import com.art.inventario.puerto.salida.AsignacionConsumiblePersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Service
public class AsignacionConsumibleAplicacion implements AsignacionConsumibleCasoDeUso {

	private final AsignacionConsumiblePersistencia persistencia;
	private final ConsumiblePersistencia consumiblePersistencia;
	private final ProyectoPersistencia proyectoPersistencia;
	private final CambiosNotificador notificador;

	public AsignacionConsumibleAplicacion(AsignacionConsumiblePersistencia persistencia,
			ConsumiblePersistencia consumiblePersistencia, ProyectoPersistencia proyectoPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.consumiblePersistencia = consumiblePersistencia;
		this.proyectoPersistencia = proyectoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<AsignacionConsumible> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<AsignacionConsumible> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public AsignacionConsumible obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public AsignacionConsumible crear(AsignacionConsumible asignacion) {
		Long proyectoId = asignacion.getProyecto() == null ? null : asignacion.getProyecto().getId();
		Long consumibleId = asignacion.getConsumible() == null ? null : asignacion.getConsumible().getId();
		validarProyecto(proyectoId);
		Consumible consumible = resolverConsumible(consumibleId);
		validarCantidad(asignacion.getCantidad());
		BigDecimal stockActual = stock(consumible);
		if (stockActual.compareTo(asignacion.getCantidad()) < 0) {
			throw new DatosInvalidosExcepcion("Stock insuficiente para asignar esa cantidad");
		}
		consumible.setStock(stockActual.subtract(asignacion.getCantidad()));
		consumiblePersistencia.guardar(consumible);
		asignacion.setConsumible(consumible);
		AsignacionConsumible creada = persistencia.guardar(asignacion);
		notificar();
		return creada;
	}

	@Override
	@Transactional
	public AsignacionConsumible actualizar(Long id, AsignacionConsumible datos) {
		AsignacionConsumible actual = persistencia.obtener(id);
		Long proyectoId = datos.getProyecto() == null ? null : datos.getProyecto().getId();
		Long consumibleId = datos.getConsumible() == null ? null : datos.getConsumible().getId();
		validarProyecto(proyectoId);
		Consumible consumible = resolverConsumible(consumibleId);
		validarCantidad(datos.getCantidad());
		Long consumibleAnteriorId = actual.getConsumible() == null ? null : actual.getConsumible().getId();
		if (consumibleAnteriorId != null && !consumibleAnteriorId.equals(consumibleId)) {
			Consumible anterior = consumiblePersistencia.obtener(consumibleAnteriorId);
			anterior.setStock(stock(anterior).add(actual.getCantidad()));
			consumiblePersistencia.guardar(anterior);
			if (stock(consumible).compareTo(datos.getCantidad()) < 0) {
				throw new DatosInvalidosExcepcion("Stock insuficiente para asignar esa cantidad");
			}
			consumible.setStock(stock(consumible).subtract(datos.getCantidad()));
			consumiblePersistencia.guardar(consumible);
		} else {
			BigDecimal ajuste = datos.getCantidad().subtract(actual.getCantidad());
			if (stock(consumible).subtract(ajuste).compareTo(BigDecimal.ZERO) < 0) {
				throw new DatosInvalidosExcepcion("Stock insuficiente para asignar esa cantidad");
			}
			consumible.setStock(stock(consumible).subtract(ajuste));
			consumiblePersistencia.guardar(consumible);
		}
		actual.setCantidad(datos.getCantidad());
		actual.setFecha(datos.getFecha());
		actual.setObservacion(datos.getObservacion());
		actual.setConsumible(consumible);
		actual.setProyecto(datos.getProyecto());
		AsignacionConsumible guardada = persistencia.guardar(actual);
		notificar();
		return guardada;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		AsignacionConsumible actual = persistencia.obtener(id);
		Consumible consumible = consumiblePersistencia.obtener(actual.getConsumible().getId());
		consumible.setStock(stock(consumible).add(actual.getCantidad()));
		consumiblePersistencia.guardar(consumible);
		persistencia.eliminar(id);
		notificar();
	}

	private void notificar() {
		notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES_CONSUMIBLES);
		notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
	}

	private Consumible resolverConsumible(Long consumibleId) {
		if (consumibleId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar un consumible");
		}
		try {
			return consumiblePersistencia.obtener(consumibleId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Consumible no encontrado");
		}
	}

	private void validarProyecto(Long proyectoId) {
		if (proyectoId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar un proyecto");
		}
		try {
			proyectoPersistencia.obtener(proyectoId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Proyecto no encontrado");
		}
	}

	private void validarCantidad(BigDecimal cantidad) {
		if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
			throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
		}
		ConsumibleAplicacion.validarPrecision(cantidad);
	}

	private static BigDecimal stock(Consumible consumible) {
		return consumible.getStock() == null ? BigDecimal.ZERO : consumible.getStock();
	}
}