package com.art.inventario.aplicacion;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.art.inventario.dominio.AlertaReposicion;
import com.art.inventario.dominio.AlertaVencimientoEpp;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Material;
import com.art.inventario.puerto.entrada.AlertaCasoDeUso;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.EntregaEppPersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;
import com.art.inventario.puerto.salida.MaterialPersistencia;

@Service
public class AlertaAplicacion implements AlertaCasoDeUso {

	private final MaterialPersistencia materiales;
	private final ConsumiblePersistencia consumibles;
	private final EppPersistencia epps;
	private final HerramientaCasoDeUso herramientas;
	private final EntregaEppPersistencia entregasEpp;

	public AlertaAplicacion(MaterialPersistencia materiales, ConsumiblePersistencia consumibles, EppPersistencia epps,
			HerramientaCasoDeUso herramientas, EntregaEppPersistencia entregasEpp) {
		this.materiales = materiales;
		this.consumibles = consumibles;
		this.epps = epps;
		this.herramientas = herramientas;
		this.entregasEpp = entregasEpp;
	}

	@Override
	public List<AlertaReposicion> listarReposicion() {
		List<AlertaReposicion> alertas = new ArrayList<>();
		for (Material m : materiales.listar()) {
			if (bajoMinimo(stock(m.getStock()), m.getStockMinimo())) {
				alertas.add(alerta("MATERIAL", m.getId(), m.getNombre(), m.getMarca(), stock(m.getStock()),
						m.getStockMinimo()));
			}
		}
		for (Consumible c : consumibles.listar()) {
			if (bajoMinimo(stock(c.getStock()), c.getStockMinimo())) {
				alertas.add(alerta("CONSUMIBLE", c.getId(), c.getNombre(), c.getMarca(), stock(c.getStock()),
						c.getStockMinimo()));
			}
		}
		for (Epp e : epps.listar()) {
			if (bajoMinimo(stock(e.getStock()), e.getStockMinimo())) {
				alertas.add(alerta("EPP", e.getId(), e.getNombre(), e.getMarca(), stock(e.getStock()), e.getStockMinimo()));
			}
		}
		for (Herramienta h : herramientas.listar()) {
			int disponible = h.getCantidadDisponible() != null ? h.getCantidadDisponible()
					: stock(h.getCantidadTotal()) - stock(h.getCantidadDanada()) - stock(h.getCantidadPerdida())
							- stock(h.getCantidadAsignada());
			if (bajoMinimo(disponible, h.getStockMinimo())) {
				alertas.add(alerta("HERRAMIENTA", h.getId(), h.getNombre(), h.getMarca(), disponible, h.getStockMinimo()));
			}
		}
		alertas.sort(Comparator.comparing(AlertaReposicion::getTipo).thenComparing(AlertaReposicion::getNombre,
				Comparator.nullsLast(String::compareToIgnoreCase)));
		return alertas;
	}

	@Override
	public List<AlertaVencimientoEpp> listarVencimientoEpp(int dias) {
		int ventana = dias < 0 ? 30 : dias;
		LocalDate hoy = LocalDate.now();
		LocalDate limite = hoy.plusDays(ventana);
		List<AlertaVencimientoEpp> alertas = new ArrayList<>();
		for (Epp e : epps.listar()) {
			if (stock(e.getStock()) <= 0) {
				continue;
			}
			LocalDate venc = parseFecha(e.getFechaVencimiento());
			if (venc == null || venc.isBefore(hoy) || venc.isAfter(limite)) {
				continue;
			}
			alertas.add(alertaVenc("EPP", e.getId(), e.getNombre(), e.getFechaVencimiento(),
					ChronoUnit.DAYS.between(hoy, venc), null));
		}
		for (EntregaEpp ent : entregasEpp.listar()) {
			LocalDate venc = parseFecha(ent.getFechaVencimiento());
			if (venc == null || venc.isBefore(hoy) || venc.isAfter(limite)) {
				continue;
			}
			String nombre = ent.getEpp() != null ? ent.getEpp().getNombre() : "Entrega EPP";
			String empleado = ent.getEmpleado() != null ? ent.getEmpleado().getNombre() : null;
			alertas.add(alertaVenc("ENTREGA", ent.getId(), nombre, ent.getFechaVencimiento(),
					ChronoUnit.DAYS.between(hoy, venc), empleado));
		}
		alertas.sort(Comparator.comparing(AlertaVencimientoEpp::getDiasRestantes)
				.thenComparing(AlertaVencimientoEpp::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)));
		return alertas;
	}

	private static LocalDate parseFecha(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(valor.trim());
		} catch (DateTimeParseException ex) {
			return null;
		}
	}

	private static boolean bajoMinimo(int stock, Integer minimo) {
		if (minimo == null || minimo <= 0) {
			return false;
		}
		return stock <= minimo;
	}

	private static int stock(Integer valor) {
		return valor == null ? 0 : valor;
	}

	private static AlertaReposicion alerta(String tipo, Long id, String nombre, String marca, int stock,
			Integer minimo) {
		AlertaReposicion a = new AlertaReposicion();
		a.setTipo(tipo);
		a.setProductoId(id);
		a.setNombre(nombre);
		a.setMarca(marca);
		a.setStock(stock);
		a.setStockMinimo(minimo);
		return a;
	}

	private static AlertaVencimientoEpp alertaVenc(String tipo, Long id, String nombre, String fecha, long dias,
			String empleado) {
		AlertaVencimientoEpp a = new AlertaVencimientoEpp();
		a.setTipo(tipo);
		a.setId(id);
		a.setNombre(nombre);
		a.setFechaVencimiento(fecha);
		a.setDiasRestantes(dias);
		a.setEmpleadoNombre(empleado);
		return a;
	}
}
