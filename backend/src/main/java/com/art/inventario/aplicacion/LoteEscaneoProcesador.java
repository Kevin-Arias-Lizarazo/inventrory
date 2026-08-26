package com.art.inventario.aplicacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.DevolucionAsignacion;
import com.art.inventario.aplicacion.dto.ItemErrorLote;
import com.art.inventario.aplicacion.dto.ItemEscaneoLote;
import com.art.inventario.aplicacion.dto.ItemPendienteLote;
import com.art.inventario.aplicacion.dto.LoteEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoLoteEscaneo;
import com.art.inventario.aplicacion.dto.TipoLoteEscaneo;
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.puerto.salida.AsignacionConsumiblePersistencia;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Service
public class LoteEscaneoProcesador {

	private final EmpleadoPersistencia empleadoPersistencia;
	private final ProyectoPersistencia proyectoPersistencia;
	private final HerramientaPersistencia herramientaPersistencia;
	private final ConsumiblePersistencia consumiblePersistencia;
	private final AsignacionHerramientaPersistencia asignacionHerramientaPersistencia;
	private final AsignacionConsumiblePersistencia asignacionConsumiblePersistencia;
	private final ContratoPersistencia contratoPersistencia;
	private final CambiosNotificador notificador;

	public LoteEscaneoProcesador(EmpleadoPersistencia empleadoPersistencia,
			ProyectoPersistencia proyectoPersistencia,
			HerramientaPersistencia herramientaPersistencia,
			ConsumiblePersistencia consumiblePersistencia,
			AsignacionHerramientaPersistencia asignacionHerramientaPersistencia,
			AsignacionConsumiblePersistencia asignacionConsumiblePersistencia,
			ContratoPersistencia contratoPersistencia,
			CambiosNotificador notificador) {
		this.empleadoPersistencia = empleadoPersistencia;
		this.proyectoPersistencia = proyectoPersistencia;
		this.herramientaPersistencia = herramientaPersistencia;
		this.consumiblePersistencia = consumiblePersistencia;
		this.asignacionHerramientaPersistencia = asignacionHerramientaPersistencia;
		this.asignacionConsumiblePersistencia = asignacionConsumiblePersistencia;
		this.contratoPersistencia = contratoPersistencia;
		this.notificador = notificador;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ResultadoLoteEscaneo procesar(LoteEscaneo lote) {
		TipoLoteEscaneo tipo = lote.getTipo();
		String destinoCodigo = lote.getDestinoCodigo();
		List<ItemErrorLote> errores = new ArrayList<>();
		List<ItemPendienteLote> pendientes = new ArrayList<>();

		// 1. Structure check
		if (lote.getItems() == null || lote.getItems().isEmpty()) {
			return new ResultadoLoteEscaneo(tipo, destinoCodigo, false, "El lote no tiene ítems", 0);
		}

		// 2. Validate destination format
		if (destinoCodigo == null || destinoCodigo.isBlank()) {
			return new ResultadoLoteEscaneo(tipo, destinoCodigo, false, "Falta el código de destino", 0);
		}

		// 3. Resolve destination
		Object destino = null;
		if (TipoLoteEscaneo.ASIGNACION.equals(tipo) || TipoLoteEscaneo.DEVOLUCION.equals(tipo)) {
			if (destinoCodigo.matches("^E\\d+$")) {
				if (!empleadoPersistencia.existePorCodigo(destinoCodigo)) {
					errores.add(new ItemErrorLote(destinoCodigo, "DESTINO_NO_REGISTRADO",
							"Destino no encontrado: " + destinoCodigo));
					return buildRejected(tipo, destinoCodigo, errores, pendientes);
				}
				destino = empleadoPersistencia.obtenerPorCodigo(destinoCodigo);
			} else if (destinoCodigo.matches("^P\\d+$") && TipoLoteEscaneo.ASIGNACION.equals(tipo)) {
				if (!proyectoPersistencia.existePorCodigo(destinoCodigo)) {
					errores.add(new ItemErrorLote(destinoCodigo, "DESTINO_NO_REGISTRADO",
							"Destino no encontrado: " + destinoCodigo));
					return buildRejected(tipo, destinoCodigo, errores, pendientes);
				}
				destino = proyectoPersistencia.obtenerPorCodigo(destinoCodigo);
			} else {
				errores.add(new ItemErrorLote(destinoCodigo, "DESTINO_NO_REGISTRADO",
						"Formato de destino inválido: " + destinoCodigo));
				return buildRejected(tipo, destinoCodigo, errores, pendientes);
			}
		}

		// 4. Destination rules
		if (TipoLoteEscaneo.ASIGNACION.equals(tipo)) {
			if (destino instanceof Empleado empleado) {
				if (!contratoPersistencia.empleadoContratado(empleado.getId())) {
					errores.add(new ItemErrorLote(destinoCodigo, "CONTRATO_INACTIVO",
							"El empleado no tiene un contrato activo"));
					return buildRejected(tipo, destinoCodigo, errores, pendientes);
				}
			} else if (destino instanceof Proyecto proyecto) {
				if (!Proyecto.ACTIVO.equals(proyecto.getEstado())) {
					errores.add(new ItemErrorLote(destinoCodigo, "PROYECTO_INACTIVO",
							"El proyecto no está activo"));
					return buildRejected(tipo, destinoCodigo, errores, pendientes);
				}
			}
		}

		// 5-6. Per-item validation, merge duplicates
		Map<String, ItemAgregado> agregados = new LinkedHashMap<>();
		String letraDestino = "E".equals(destinoCodigo.substring(0, 1)) ? "E" : "P";
		String letraItemEsperada = TipoLoteEscaneo.ASIGNACION.equals(tipo)
				? ("E".equals(letraDestino) ? "H" : "C")
				: "H";

		for (ItemEscaneoLote item : lote.getItems()) {
			if (item.getCodigo() == null || !item.getCodigo().matches("^[HC]\\d+$")) {
				errores.add(new ItemErrorLote(item.getCodigo(), "TIPO_CRUZADO",
						"Código inválido para el destino"));
				continue;
			}
			String letraItem = item.getCodigo().substring(0, 1);
			if (!letraItem.equals(letraItemEsperada)) {
				errores.add(new ItemErrorLote(item.getCodigo(), "TIPO_CRUZADO",
						"Código inválido para el destino"));
				continue;
			}
			if (item.getCantidad() == null || item.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
				errores.add(new ItemErrorLote(item.getCodigo(), "CANTIDAD_INVALIDA",
						"La cantidad debe ser mayor a cero"));
				continue;
			}
			// Precision check
			if ("H".equals(letraItem)) {
				if (item.getCantidad().stripTrailingZeros().scale() > 0) {
					errores.add(new ItemErrorLote(item.getCodigo(), "CANTIDAD_INVALIDA",
							"La cantidad de herramientas debe ser un entero"));
					continue;
				}
			} else {
				if (item.getCantidad().stripTrailingZeros().scale() > 1) {
					errores.add(new ItemErrorLote(item.getCodigo(), "CANTIDAD_INVALIDA",
							"La cantidad admite máximo un decimal"));
					continue;
				}
			}

			// Existence check
			if ("H".equals(letraItem)) {
				if (!herramientaPersistencia.existePorCodigo(item.getCodigo())) {
					pendientes.add(new ItemPendienteLote(item.getCodigo(), "ITEM_NO_REGISTRADO",
							"Ítem no registrado: " + item.getCodigo()));
					continue;
				}
			} else {
				if (!consumiblePersistencia.existePorCodigo(item.getCodigo())) {
					pendientes.add(new ItemPendienteLote(item.getCodigo(), "ITEM_NO_REGISTRADO",
							"Ítem no registrado: " + item.getCodigo()));
					continue;
				}
			}

			// Merge duplicates
			agregados.merge(item.getCodigo(), new ItemAgregado(item.getCodigo(), item.getCantidad(), letraItem, item.getAsignaciones()),
					(a, b) -> {
						a.cantidad = a.cantidad.add(b.cantidad);
						if (b.asignaciones != null) {
							if (a.asignaciones == null) a.asignaciones = new ArrayList<>();
							a.asignaciones.addAll(b.asignaciones);
						}
						return a;
					});
		}

		// Blocking: pending items block the whole lot
		if (!pendientes.isEmpty()) {
			ResultadoLoteEscaneo pendienteResultado = new ResultadoLoteEscaneo(tipo, destinoCodigo, false,
					"Lote pendiente de acomodar (" + pendientes.size() + " ítem(s) no registrado(s))",
					0);
			pendienteResultado.setPendientes(pendientes);
			return pendienteResultado;
		}

		// Structure errors block too
		if (!errores.isEmpty()) {
			return buildRejected(tipo, destinoCodigo, errores, pendientes);
		}

		// 7. Availability per aggregated item
		if (TipoLoteEscaneo.ASIGNACION.equals(tipo)) {
			for (ItemAgregado ag : agregados.values()) {
				if ("H".equals(ag.letra)) {
					Herramienta h = herramientaPersistencia.obtenerPorCodigo(ag.codigo);
					int total = h.getCantidadTotal() == null ? 0 : h.getCantidadTotal();
					int danada = h.getCantidadDanada() == null ? 0 : h.getCantidadDanada();
					int perdida = h.getCantidadPerdida() == null ? 0 : h.getCantidadPerdida();
					long asignada = asignacionHerramientaPersistencia.contarAsignacionesActivas(h.getId(), -1L);
					if (total - (int) asignada - danada - perdida < ag.cantidad.intValue()) {
						errores.add(new ItemErrorLote(ag.codigo, "SIN_DISPONIBILIDAD",
								"No hay unidades disponibles de " + h.getNombre()));
					}
				} else {
					Consumible c = consumiblePersistencia.obtenerPorCodigo(ag.codigo);
					BigDecimal stockActual = c.getStock() == null ? BigDecimal.ZERO : c.getStock();
					if (stockActual.compareTo(ag.cantidad) < 0) {
						errores.add(new ItemErrorLote(ag.codigo, "STOCK_INSUFICIENTE",
								"Stock insuficiente de " + c.getNombre()));
					}
				}
			}
			if (!errores.isEmpty()) {
				return buildRejected(tipo, destinoCodigo, errores, pendientes);
			}
		}

		// 8. DEVOLUCION pre-flight coverage
		if (TipoLoteEscaneo.DEVOLUCION.equals(tipo)) {
			Empleado empleado = (Empleado) destino;
			for (ItemAgregado ag : agregados.values()) {
				Herramienta herramienta = herramientaPersistencia.obtenerPorCodigo(ag.codigo);
				List<AsignacionHerramienta> filasAbiertas = asignacionHerramientaPersistencia
						.activasMasAntiguas(empleado.getId(), herramienta.getId(), Integer.MAX_VALUE);

				if (ag.asignaciones != null && !ag.asignaciones.isEmpty()) {
					// Explicit remap validation
					BigDecimal remapSum = BigDecimal.ZERO;
					boolean remapValid = true;
					for (DevolucionAsignacion da : ag.asignaciones) {
						boolean found = filasAbiertas.stream()
								.anyMatch(f -> f.getId().equals(da.getId()) && f.getCantidad() != null && f.getCantidad() > 0);
						if (!found) {
							remapValid = false;
							break;
						}
						if (da.getCantidad() == null || da.getCantidad().stripTrailingZeros().scale() > 0) {
							remapValid = false;
							break;
						}
						remapSum = remapSum.add(da.getCantidad());
					}
					if (!remapValid || remapSum.compareTo(ag.cantidad) != 0) {
						errores.add(new ItemErrorLote(ag.codigo, "ASIGNACION_REMAP_INVALIDA",
								"La distribución de devolución no es válida"));
					}
				} else {
					// FIFO coverage check
					long sumAbiertas = filasAbiertas.stream()
							.filter(f -> f.getCantidad() != null && f.getCantidad() > 0)
							.mapToLong(AsignacionHerramienta::getCantidad)
							.sum();
					if (sumAbiertas < ag.cantidad.intValue()) {
						errores.add(new ItemErrorLote(ag.codigo, "EXCESO_DEVOLUCION",
								"No hay suficientes asignaciones activas para devolver"));
					}
				}
			}
			if (!errores.isEmpty()) {
				return buildRejected(tipo, destinoCodigo, errores, pendientes);
			}
		}

		// 9. APPLY phase
		int registros = 0;
		if (TipoLoteEscaneo.ASIGNACION.equals(tipo)) {
			registros = applyAsignacion(destino, agregados);
		} else {
			registros = applyDevolucion(destino, agregados);
		}

		// 10. Single SSE per lot
		if (TipoLoteEscaneo.ASIGNACION.equals(tipo)) {
			if (destino instanceof Empleado) {
				notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES);
				notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
			} else {
				notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES_CONSUMIBLES);
				notificador.publicar(CambiosNotificador.RECURSO_CONSUMIBLES);
			}
		} else {
			notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES);
			notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		}

		return new ResultadoLoteEscaneo(tipo, destinoCodigo, true,
				"Lote procesado correctamente (" + registros + " registro(s))", registros);
	}

	private int applyAsignacion(Object destino, Map<String, ItemAgregado> agregados) {
		int registros = 0;
		String fecha = LocalDate.now().toString();
		for (ItemAgregado ag : agregados.values()) {
			if ("H".equals(ag.letra)) {
				Empleado empleado = (Empleado) destino;
				Herramienta herramienta = herramientaPersistencia.obtenerPorCodigo(ag.codigo);
				AsignacionHerramienta a = new AsignacionHerramienta();
				a.setEmpleado(empleado);
				a.setHerramienta(herramienta);
				a.setFecha(fecha);
				a.setDevuelta(false);
				a.setLugar("");
				a.setCantidad(ag.cantidad.intValue());
				asignacionHerramientaPersistencia.guardar(a);
				registros++;
			} else {
				Proyecto proyecto = (Proyecto) destino;
				Consumible consumible = consumiblePersistencia.obtenerPorCodigo(ag.codigo);
				AsignacionConsumible a = new AsignacionConsumible();
				a.setConsumible(consumible);
				a.setProyecto(proyecto);
				a.setCantidad(ag.cantidad);
				a.setFecha(fecha);
				a.setObservacion("");
				asignacionConsumiblePersistencia.guardar(a);
				consumible.setStock(consumible.getStock().subtract(ag.cantidad));
				consumiblePersistencia.guardar(consumible);
				registros++;
			}
		}
		return registros;
	}

	private int applyDevolucion(Object destino, Map<String, ItemAgregado> agregados) {
		Empleado empleado = (Empleado) destino;
		int registros = 0;
		String hoy = LocalDate.now().toString();
		for (ItemAgregado ag : agregados.values()) {
			Herramienta herramienta = herramientaPersistencia.obtenerPorCodigo(ag.codigo);
			List<AsignacionHerramienta> filasAbiertas = asignacionHerramientaPersistencia
					.activasMasAntiguas(empleado.getId(), herramienta.getId(), Integer.MAX_VALUE);

			if (ag.asignaciones != null && !ag.asignaciones.isEmpty()) {
				// Explicit remap
				for (DevolucionAsignacion da : ag.asignaciones) {
					AsignacionHerramienta fila = asignacionHerramientaPersistencia.obtener(da.getId());
					int nuevaCantidad = fila.getCantidad() - da.getCantidad().intValue();
					fila.setCantidad(nuevaCantidad);
					if (nuevaCantidad == 0) {
						fila.setDevuelta(true);
						fila.setFechaDevolucion(hoy);
					}
					asignacionHerramientaPersistencia.guardar(fila);
					registros++;
				}
			} else {
				// FIFO
				int restante = ag.cantidad.intValue();
				for (AsignacionHerramienta fila : filasAbiertas) {
					if (restante <= 0) break;
					if (fila.getCantidad() == null || fila.getCantidad() <= 0) continue;
					int tomar = Math.min(restante, fila.getCantidad());
					fila.setCantidad(fila.getCantidad() - tomar);
					restante -= tomar;
					if (fila.getCantidad() == 0) {
						fila.setDevuelta(true);
						fila.setFechaDevolucion(hoy);
					}
					asignacionHerramientaPersistencia.guardar(fila);
					registros++;
				}
			}
		}
		return registros;
	}

	private ResultadoLoteEscaneo buildRejected(TipoLoteEscaneo tipo, String destinoCodigo,
			List<ItemErrorLote> errores, List<ItemPendienteLote> pendientes) {
		ResultadoLoteEscaneo resultado = new ResultadoLoteEscaneo(tipo, destinoCodigo, false, "Lote rechazado", 0);
		resultado.setErrores(errores);
		resultado.setPendientes(pendientes);
		return resultado;
	}

	// Internal merge key
	static class ItemAgregado {
		String codigo;
		BigDecimal cantidad;
		String letra;
		List<DevolucionAsignacion> asignaciones;

		ItemAgregado(String codigo, BigDecimal cantidad, String letra, List<DevolucionAsignacion> asignaciones) {
			this.codigo = codigo;
			this.cantidad = cantidad;
			this.letra = letra;
			this.asignaciones = asignaciones;
		}
	}
}
