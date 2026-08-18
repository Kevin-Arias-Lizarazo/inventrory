package com.art.inventario.aplicacion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Ajuste;
import com.art.inventario.dominio.LineaAjuste;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.AjusteCasoDeUso;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;
import com.art.inventario.puerto.entrada.EppCasoDeUso;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.entrada.MaterialCasoDeUso;
import com.art.inventario.puerto.salida.AjustePersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;

@Service
public class AjusteAplicacion implements AjusteCasoDeUso {

	private final AjustePersistencia persistencia;
	private final HerramientaCasoDeUso herramientas;
	private final EppCasoDeUso epps;
	private final ConsumibleCasoDeUso consumibles;
	private final MaterialCasoDeUso materiales;
	private final CambiosNotificador notificador;

	public AjusteAplicacion(AjustePersistencia persistencia, HerramientaCasoDeUso herramientas, EppCasoDeUso epps,
			ConsumibleCasoDeUso consumibles, MaterialCasoDeUso materiales, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.herramientas = herramientas;
		this.epps = epps;
		this.consumibles = consumibles;
		this.materiales = materiales;
		this.notificador = notificador;
	}

	@Override
	public List<Ajuste> listar() {
		return persistencia.listar();
	}

	@Override
	public Ajuste obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Ajuste crear(Ajuste ajuste) {
		validarAjuste(ajuste);
		validarProductos(ajuste.getLineas());
		normalizarCantidadesDisponibles(ajuste.getLineas());
		Ajuste creado = persistencia.guardar(ajuste);
		aplicarMovimientos(creado.getId(), creado.getLineas(), creado.getFecha());
		notificar(creado.getLineas());
		return obtener(creado.getId());
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Ajuste actual = persistencia.obtener(id);
		revertirMovimientos(id, actual.getLineas());
		persistencia.eliminar(id);
		notificar(actual.getLineas());
	}

	private void validarAjuste(Ajuste ajuste) {
		if (ajuste.getFecha() == null || ajuste.getFecha().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha es obligatoria");
		}
		validarMotivo(ajuste.getMotivo());
		if (ajuste.getLineas() == null || ajuste.getLineas().isEmpty()) {
			throw new DatosInvalidosExcepcion("Debe agregar al menos una línea");
		}
		for (LineaAjuste linea : ajuste.getLineas()) {
			validarTipoProducto(linea.getTipoProducto());
			if (linea.getProductoId() == null) {
				throw new DatosInvalidosExcepcion("Seleccione un producto válido");
			}
			if (linea.getCantidadDisponible() != null) {
				if (linea.getCantidadDisponible() < 0) {
					throw new DatosInvalidosExcepcion("La cantidad disponible no puede ser negativa");
				}
			} else {
				if (linea.getCantidad() == null || linea.getCantidad() <= 0) {
					throw new DatosInvalidosExcepcion("La cantidad debe ser mayor a cero");
				}
				validarTipoMovimiento(linea.getTipoMovimiento());
			}
		}
	}

	private void normalizarCantidadesDisponibles(List<LineaAjuste> lineas) {
		for (LineaAjuste linea : lineas) {
			if (linea.getCantidadDisponible() == null) {
				continue;
			}
			int disponibleActual = disponible(linea.getTipoProducto(), linea.getProductoId());
			int diferencia = linea.getCantidadDisponible() - disponibleActual;
			if (diferencia == 0) {
				throw new DatosInvalidosExcepcion("La cantidad disponible ya coincide con el inventario actual");
			}
			linea.setTipoMovimiento(diferencia > 0 ? Ajuste.MOV_INGRESO : Ajuste.MOV_EGRESO);
			linea.setCantidad(Math.abs(diferencia));
		}
	}

	private int disponible(String tipoProducto, Long productoId) {
		return switch (tipoProducto) {
		case Ajuste.TIPO_HERRAMIENTA -> disponible(herramientas.obtener(productoId));
		case Ajuste.TIPO_EPP -> stock(epps.obtener(productoId));
		case Ajuste.TIPO_CONSUMIBLE -> stock(consumibles.obtener(productoId));
		case Ajuste.TIPO_MATERIAL -> stock(materiales.obtener(productoId));
		default -> 0;
		};
	}

	private static int disponible(Herramienta herramienta) {
		return herramienta.getCantidadDisponible() == null ? 0 : herramienta.getCantidadDisponible();
	}

	private static int stock(Epp epp) {
		return epp.getStock() == null ? 0 : epp.getStock();
	}

	private static int stock(Consumible consumible) {
		return consumible.getStock() == null ? 0 : consumible.getStock();
	}

	private static int stock(Material material) {
		return material.getStock() == null ? 0 : material.getStock();
	}

	private void validarMotivo(String motivo) {
		if (!Ajuste.MOTIVO_CONTEO.equals(motivo) && !Ajuste.MOTIVO_MERMA.equals(motivo)
				&& !Ajuste.MOTIVO_SOBRANTE.equals(motivo) && !Ajuste.MOTIVO_DANO.equals(motivo)) {
			throw new DatosInvalidosExcepcion("Motivo no válido: " + motivo);
		}
	}

	private void validarTipoMovimiento(String tipo) {
		if (!Ajuste.MOV_INGRESO.equals(tipo) && !Ajuste.MOV_EGRESO.equals(tipo)) {
			throw new DatosInvalidosExcepcion("Tipo de movimiento no válido: " + tipo);
		}
	}

	private void validarTipoProducto(String tipo) {
		if (!Ajuste.TIPO_HERRAMIENTA.equals(tipo) && !Ajuste.TIPO_EPP.equals(tipo)
				&& !Ajuste.TIPO_CONSUMIBLE.equals(tipo) && !Ajuste.TIPO_MATERIAL.equals(tipo)) {
			throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + tipo);
		}
	}

	private void validarProductos(List<LineaAjuste> lineas) {
		for (LineaAjuste linea : lineas) {
			switch (linea.getTipoProducto()) {
			case Ajuste.TIPO_HERRAMIENTA -> herramientas.obtener(linea.getProductoId());
			case Ajuste.TIPO_EPP -> epps.obtener(linea.getProductoId());
			case Ajuste.TIPO_CONSUMIBLE -> consumibles.obtener(linea.getProductoId());
			case Ajuste.TIPO_MATERIAL -> materiales.obtener(linea.getProductoId());
			default -> throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + linea.getTipoProducto());
			}
		}
	}

	private void aplicarMovimientos(Long ajusteId, List<LineaAjuste> lineas, String fecha) {
		String etiqueta = etiqueta(ajusteId);
		for (LineaAjuste linea : lineas) {
			registrarMovimiento(linea.getTipoProducto(), linea.getProductoId(), linea.getTipoMovimiento(),
					linea.getCantidad(), fecha, etiqueta);
		}
	}

	private void revertirMovimientos(Long ajusteId, List<LineaAjuste> lineas) {
		String etiqueta = etiqueta(ajusteId);
		for (LineaAjuste linea : lineas) {
			eliminarMovimientosConEtiqueta(linea.getTipoProducto(), linea.getProductoId(), etiqueta);
		}
	}

	private void registrarMovimiento(String tipoProducto, Long productoId, String tipoMovimiento, Integer cantidad,
			String fecha, String etiqueta) {
		switch (tipoProducto) {
		case Ajuste.TIPO_HERRAMIENTA -> {
			MovimientoHerramienta m = new MovimientoHerramienta();
			m.setTipo(tipoMovimiento);
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			herramientas.registrarMovimiento(productoId, m);
		}
		case Ajuste.TIPO_EPP -> {
			MovimientoEpp m = new MovimientoEpp();
			m.setTipo(tipoMovimiento);
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			epps.registrarMovimiento(productoId, m);
		}
		case Ajuste.TIPO_CONSUMIBLE -> {
			MovimientoConsumible m = new MovimientoConsumible();
			m.setTipo(tipoMovimiento);
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			consumibles.registrarMovimiento(productoId, m);
		}
		case Ajuste.TIPO_MATERIAL -> {
			MovimientoMaterial m = new MovimientoMaterial();
			m.setTipo(tipoMovimiento);
			m.setCantidad(cantidad);
			m.setFecha(fecha);
			m.setObservacion(etiqueta);
			materiales.registrarMovimiento(productoId, m);
		}
		default -> {
		}
		}
	}

	private void eliminarMovimientosConEtiqueta(String tipoProducto, Long productoId, String etiqueta) {
		switch (tipoProducto) {
		case Ajuste.TIPO_HERRAMIENTA -> herramientas.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> herramientas.eliminarMovimiento(m.getId()));
		case Ajuste.TIPO_EPP -> epps.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion())).forEach(m -> epps.eliminarMovimiento(m.getId()));
		case Ajuste.TIPO_CONSUMIBLE -> consumibles.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> consumibles.eliminarMovimiento(m.getId()));
		case Ajuste.TIPO_MATERIAL -> materiales.listarMovimientos(productoId).stream()
				.filter(m -> etiqueta.equals(m.getObservacion()))
				.forEach(m -> materiales.eliminarMovimiento(m.getId()));
		default -> {
		}
		}
	}

	private void notificar(List<LineaAjuste> lineas) {
		notificador.publicar(CambiosNotificador.RECURSO_AJUSTES);
		Set<String> recursos = new HashSet<>();
		for (LineaAjuste linea : lineas) {
			switch (linea.getTipoProducto()) {
			case Ajuste.TIPO_HERRAMIENTA -> recursos.add(CambiosNotificador.RECURSO_HERRAMIENTAS);
			case Ajuste.TIPO_EPP -> recursos.add(CambiosNotificador.RECURSO_EPP);
			case Ajuste.TIPO_CONSUMIBLE -> recursos.add(CambiosNotificador.RECURSO_CONSUMIBLES);
			case Ajuste.TIPO_MATERIAL -> recursos.add(CambiosNotificador.RECURSO_MATERIALES);
			default -> {
			}
			}
		}
		recursos.forEach(notificador::publicar);
	}

	private static String etiqueta(Long ajusteId) {
		return "Ajuste #" + ajusteId;
	}
}
