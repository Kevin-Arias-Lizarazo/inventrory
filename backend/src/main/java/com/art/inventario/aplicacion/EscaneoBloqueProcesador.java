package com.art.inventario.aplicacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.BloqueEscaneo;
import com.art.inventario.aplicacion.dto.ItemEscaneo;
import com.art.inventario.aplicacion.dto.ResultadoBloque;
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.AsignacionConsumibleCasoDeUso;
import com.art.inventario.puerto.entrada.AsignacionHerramientaCasoDeUso;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Service
public class EscaneoBloqueProcesador {

	private static final Set<String> OPERACIONES = Set.of("AH", "DH", "AC");

	private final EmpleadoPersistencia empleadoPersistencia;
	private final ProyectoPersistencia proyectoPersistencia;
	private final ConsumiblePersistencia consumiblePersistencia;
	private final HerramientaPersistencia herramientaPersistencia;
	private final AsignacionHerramientaPersistencia asignacionHerramientaPersistencia;
	private final AsignacionHerramientaCasoDeUso asignacionHerramientaCasoDeUso;
	private final AsignacionConsumibleCasoDeUso asignacionConsumibleCasoDeUso;
	private final CambiosNotificador notificador;

	public EscaneoBloqueProcesador(EmpleadoPersistencia empleadoPersistencia,
			ProyectoPersistencia proyectoPersistencia, ConsumiblePersistencia consumiblePersistencia,
			HerramientaPersistencia herramientaPersistencia,
			AsignacionHerramientaPersistencia asignacionHerramientaPersistencia,
			AsignacionHerramientaCasoDeUso asignacionHerramientaCasoDeUso,
			AsignacionConsumibleCasoDeUso asignacionConsumibleCasoDeUso, CambiosNotificador notificador) {
		this.empleadoPersistencia = empleadoPersistencia;
		this.proyectoPersistencia = proyectoPersistencia;
		this.consumiblePersistencia = consumiblePersistencia;
		this.herramientaPersistencia = herramientaPersistencia;
		this.asignacionHerramientaPersistencia = asignacionHerramientaPersistencia;
		this.asignacionHerramientaCasoDeUso = asignacionHerramientaCasoDeUso;
		this.asignacionConsumibleCasoDeUso = asignacionConsumibleCasoDeUso;
		this.notificador = notificador;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ResultadoBloque procesar(BloqueEscaneo bloque) {
		validar(bloque);
		String operacion = bloque.getOperacion();
		int registros = 0;
		if ("AH".equals(operacion)) {
			registros = asignarHerramientas(bloque);
		} else if ("DH".equals(operacion)) {
			registros = devolverHerramientas(bloque);
		} else {
			registros = asignarConsumibles(bloque);
		}
		return new ResultadoBloque(operacion, bloque.getDestinoCodigo(), true,
				"Procesado correctamente (" + registros + " registro(s))", registros);
	}

	private int asignarHerramientas(BloqueEscaneo bloque) {
		Empleado empleado = empleadoPersistencia.obtenerPorCodigo(bloque.getDestinoCodigo());
		int creados = 0;
		for (ItemEscaneo item : bloque.getItems()) {
			Herramienta herramienta = herramientaPersistencia.obtenerPorCodigo(item.getCodigo());
			for (int i = 0; i < cantidad(item); i++) {
				AsignacionHerramienta asignacion = new AsignacionHerramienta();
				asignacion.setEmpleado(empleado);
				asignacion.setHerramienta(herramienta);
				asignacion.setFecha(hoy());
				asignacion.setDevuelta(false);
				asignacion.setLugar("");
				asignacion.setCantidad(1);
				asignacionHerramientaCasoDeUso.crear(asignacion);
				creados++;
			}
		}
		return creados;
	}

	private int devolverHerramientas(BloqueEscaneo bloque) {
		Empleado empleado = empleadoPersistencia.obtenerPorCodigo(bloque.getDestinoCodigo());
		int devueltas = 0;
		for (ItemEscaneo item : bloque.getItems()) {
			Herramienta herramienta = herramientaPersistencia.obtenerPorCodigo(item.getCodigo());
			int cantidadDevolver = cantidad(item);
			List<AsignacionHerramienta> filasAbiertas = asignacionHerramientaPersistencia
					.activasMasAntiguas(empleado.getId(), herramienta.getId(), Integer.MAX_VALUE);
			long sumAbiertas = filasAbiertas.stream()
					.filter(a -> a.getCantidad() != null && a.getCantidad() > 0)
					.mapToLong(AsignacionHerramienta::getCantidad)
					.sum();
			if (sumAbiertas < cantidadDevolver) {
				throw new DatosInvalidosExcepcion(
						"El empleado no tiene suficientes asignaciones activas de \"" + herramienta.getNombre() + "\"");
			}
			int restante = cantidadDevolver;
			for (AsignacionHerramienta a : filasAbiertas) {
				if (restante <= 0) break;
				if (a.getCantidad() == null || a.getCantidad() <= 0) continue;
				int tomar = Math.min(restante, a.getCantidad());
				a.setCantidad(a.getCantidad() - tomar);
				restante -= tomar;
				if (a.getCantidad() == 0) {
					a.setDevuelta(true);
					a.setFechaDevolucion(hoy());
				}
				asignacionHerramientaPersistencia.guardar(a);
				devueltas++;
			}
		}
		notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
		return devueltas;
	}

	private int asignarConsumibles(BloqueEscaneo bloque) {
		Proyecto proyecto = proyectoPersistencia.obtenerPorCodigo(bloque.getDestinoCodigo());
		int creados = 0;
		for (ItemEscaneo item : bloque.getItems()) {
			Consumible consumible = consumiblePersistencia.obtenerPorCodigo(item.getCodigo());
			AsignacionConsumible asignacion = new AsignacionConsumible();
			asignacion.setConsumible(consumible);
			asignacion.setProyecto(proyecto);
			asignacion.setCantidad(BigDecimal.valueOf(cantidad(item)));
			asignacion.setFecha(hoy());
			asignacionConsumibleCasoDeUso.crear(asignacion);
			creados++;
		}
		return creados;
	}

	private void validar(BloqueEscaneo bloque) {
		if (bloque.getOperacion() == null || !OPERACIONES.contains(bloque.getOperacion())) {
			throw new DatosInvalidosExcepcion("Código de operación inválido");
		}
		if (bloque.getItems() == null || bloque.getItems().isEmpty()) {
			throw new DatosInvalidosExcepcion("El bloque no tiene ítems");
		}
		if (bloque.getDestinoCodigo() == null || bloque.getDestinoCodigo().isBlank()) {
			throw new DatosInvalidosExcepcion("Falta el código de destino");
		}
		String letraDestino = "AH".equals(bloque.getOperacion()) || "DH".equals(bloque.getOperacion()) ? "E" : "P";
		validarFormato(bloque.getDestinoCodigo(), letraDestino);
		String letraItem = "AC".equals(bloque.getOperacion()) ? "C" : "H";
		for (ItemEscaneo item : bloque.getItems()) {
			if (item.getCodigo() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
				throw new DatosInvalidosExcepcion("Ítem con código o cantidad inválida");
			}
			validarFormato(item.getCodigo(), letraItem);
		}
	}

	private void validarFormato(String codigo, String letra) {
		if (codigo == null || !codigo.matches("^" + letra + "\\d+$")) {
			throw new DatosInvalidosExcepcion("Código inválido: \"" + codigo + "\"");
		}
	}

	private static int cantidad(ItemEscaneo item) {
		return item.getCantidad() == null ? 1 : item.getCantidad();
	}

	private static String hoy() {
		return LocalDate.now().toString();
	}
}