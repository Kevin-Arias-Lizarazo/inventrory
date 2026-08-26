package com.art.inventario.aplicacion;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.CreacionExpressEscaneo;
import com.art.inventario.aplicacion.dto.IncrementoStockEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoExpress;
import com.art.inventario.aplicacion.dto.TipoProductoExpress;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.EscaneoExpressCasoDeUso;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;

@Service
public class EscaneoExpressAplicacion implements EscaneoExpressCasoDeUso {

	private final HerramientaPersistencia herramientaPersistencia;
	private final ConsumiblePersistencia consumiblePersistencia;
	private final HerramientaAplicacion herramientaAplicacion;
	private final ConsumibleAplicacion consumibleAplicacion;

	public EscaneoExpressAplicacion(HerramientaPersistencia herramientaPersistencia,
			ConsumiblePersistencia consumiblePersistencia,
			HerramientaAplicacion herramientaAplicacion,
			ConsumibleAplicacion consumibleAplicacion) {
		this.herramientaPersistencia = herramientaPersistencia;
		this.consumiblePersistencia = consumiblePersistencia;
		this.herramientaAplicacion = herramientaAplicacion;
		this.consumibleAplicacion = consumibleAplicacion;
	}

	@Override
	@Transactional
	public ResultadoExpress incrementarStock(IncrementoStockEscaneo request) {
		if (request.getCodigo() == null || request.getCodigo().isBlank()) {
			throw new DatosInvalidosExcepcion("El código es obligatorio");
		}
		if (!request.getCodigo().matches("^[HC]\\d+$")) {
			throw new DatosInvalidosExcepcion("Formato de código inválido: " + request.getCodigo());
		}
		if (request.getCantidad() == null || request.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
			throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
		}
		validarPrecision(request.getCantidad());

		String letra = request.getCodigo().substring(0, 1);
		if ("H".equals(letra)) {
			Herramienta herramienta = herramientaPersistencia.obtenerPorCodigo(request.getCodigo());
			MovimientoHerramienta movimiento = new MovimientoHerramienta();
			movimiento.setTipo("INGRESO");
			movimiento.setCantidad(request.getCantidad().intValue());
			movimiento.setFecha(java.time.LocalDate.now().toString());
			movimiento.setObservacion("Incremento por escaneo");
			herramientaAplicacion.registrarMovimiento(herramienta.getId(), movimiento);
			return new ResultadoExpress(true, "Stock incrementado", herramienta.getId(), herramienta.getCodigo());
		} else {
			Consumible consumible = consumiblePersistencia.obtenerPorCodigo(request.getCodigo());
			MovimientoConsumible movimiento = new MovimientoConsumible();
			movimiento.setTipo("INGRESO");
			movimiento.setCantidad(request.getCantidad());
			movimiento.setFecha(java.time.LocalDate.now().toString());
			movimiento.setObservacion("Incremento por escaneo");
			consumibleAplicacion.registrarMovimiento(consumible.getId(), movimiento);
			return new ResultadoExpress(true, "Stock incrementado", consumible.getId(), consumible.getCodigo());
		}
	}

	@Override
	@Transactional
	public ResultadoExpress crearItem(CreacionExpressEscaneo request) {
		if (request.getTipo() == null) {
			throw new DatosInvalidosExcepcion("El tipo es obligatorio");
		}
		if (request.getCodigo() == null || request.getCodigo().isBlank()) {
			throw new DatosInvalidosExcepcion("El código es obligatorio");
		}
		if (!request.getCodigo().matches("^[HC]\\d+$")) {
			throw new DatosInvalidosExcepcion("Formato de código inválido: " + request.getCodigo());
		}
		if (request.getNombre() == null || request.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
		if (request.getMarca() == null || request.getMarca().isBlank()) {
			throw new DatosInvalidosExcepcion("La marca es obligatoria");
		}

		String letra = request.getCodigo().substring(0, 1);
		if (TipoProductoExpress.HERRAMIENTA.equals(request.getTipo())) {
			if (!"H".equals(letra)) {
				throw new DatosInvalidosExcepcion(
						"Código de herramienta inválido: debe empezar con H");
			}
			if (request.getCantidadTotal() == null || request.getCantidadTotal() < 1) {
				throw new DatosInvalidosExcepcion("La cantidad total debe ser mayor a cero");
			}
			Herramienta herramienta = new Herramienta();
			herramienta.setCodigo(request.getCodigo());
			herramienta.setNombre(request.getNombre());
			herramienta.setMarca(request.getMarca());
			herramienta.setCantidadTotal(request.getCantidadTotal());
			Herramienta creada;
			try {
				creada = herramientaAplicacion.crearConCodigo(herramienta);
			} catch (ConflictoExcepcion e) {
				throw new DatosInvalidosExcepcion(e.getMessage());
			}
			return new ResultadoExpress(true, "Ítem creado", creada.getId(), creada.getCodigo());
		} else if (TipoProductoExpress.CONSUMIBLE.equals(request.getTipo())) {
			if (!"C".equals(letra)) {
				throw new DatosInvalidosExcepcion(
						"Código de consumible inválido: debe empezar con C");
			}
			if (request.getUnidad() == null || request.getUnidad().isBlank()) {
				throw new DatosInvalidosExcepcion("La unidad es obligatoria");
			}
			Consumible consumible = new Consumible();
			consumible.setCodigo(request.getCodigo());
			consumible.setNombre(request.getNombre());
			consumible.setMarca(request.getMarca());
			consumible.setUnidad(request.getUnidad());
			Consumible creadoConsumible;
			try {
				creadoConsumible = consumibleAplicacion.crearConCodigo(consumible);
			} catch (ConflictoExcepcion e) {
				throw new DatosInvalidosExcepcion(e.getMessage());
			}
			return new ResultadoExpress(true, "Ítem creado", creadoConsumible.getId(), creadoConsumible.getCodigo());
		} else {
			throw new DatosInvalidosExcepcion("Tipo no válido: " + request.getTipo());
		}
	}

	private void validarPrecision(BigDecimal cantidad) {
		BigDecimal stripped = cantidad.stripTrailingZeros();
		if (stripped.scale() > 1) {
			throw new DatosInvalidosExcepcion("La cantidad admite máximo un decimal");
		}
	}
}
