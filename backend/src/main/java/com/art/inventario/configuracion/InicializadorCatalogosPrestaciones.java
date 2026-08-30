package com.art.inventario.configuracion;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.dominio.TipoContratoPrestacion;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;
import com.art.inventario.puerto.salida.PrestacionPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPrestacionPersistencia;

/**
 * Idempotent seed for the Fase A catalogs: contract types, benefits
 * (prestaciones) grouped by category, the contract-type/benefit matrix and the
 * 2026 legal parameters. Mirrors the insert-if-absent pattern of
 * {@link InicializadorNivelesAcceso}: it only inserts rows whose natural key is
 * missing, so re-runs on an already-seeded database insert nothing.
 */
@Component
@Order(2)
public class InicializadorCatalogosPrestaciones implements ApplicationRunner {

	private final TipoContratoPersistencia tipos;
	private final PrestacionPersistencia prestaciones;
	private final TipoContratoPrestacionPersistencia matriz;
	private final ParametroLegalPersistencia parametros;

	public InicializadorCatalogosPrestaciones(TipoContratoPersistencia tipos, PrestacionPersistencia prestaciones,
			TipoContratoPrestacionPersistencia matriz, ParametroLegalPersistencia parametros) {
		this.tipos = tipos;
		this.prestaciones = prestaciones;
		this.matriz = matriz;
		this.parametros = parametros;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<TipoContrato> tiposExistentes = tipos.listar();
		Set<String> nombresTipos = tiposExistentes.stream().map(TipoContrato::getNombre).collect(Collectors.toSet());
		Map<String, TipoContrato> tiposPorNombre = new LinkedHashMap<>();
		for (TipoContrato t : datosTipos()) {
			TipoContrato registro;
			if (nombresTipos.contains(t.getNombre())) {
				registro = tiposExistentes.stream()
						.filter(x -> x.getNombre().equals(t.getNombre()))
						.findFirst()
						.orElse(t);
			} else {
				registro = tipos.guardar(t);
			}
			tiposPorNombre.put(registro.getNombre(), registro);
		}

		List<Prestacion> prestacionesExistentes = prestaciones.listar();
		Set<String> nombresPrestaciones = prestacionesExistentes.stream()
				.map(Prestacion::getNombre)
				.collect(Collectors.toSet());
		Map<String, Prestacion> prestacionesPorNombre = new LinkedHashMap<>();
		for (Prestacion p : datosPrestaciones()) {
			Prestacion registro;
			if (nombresPrestaciones.contains(p.getNombre())) {
				registro = prestacionesExistentes.stream()
						.filter(x -> x.getNombre().equals(p.getNombre()))
						.findFirst()
						.orElse(p);
			} else {
				registro = prestaciones.guardar(p);
			}
			prestacionesPorNombre.put(registro.getNombre(), registro);
		}

		sembrarMatriz(tiposPorNombre, prestacionesPorNombre);

		if (!parametros.existeAnio(2026)) {
			parametros.guardar(parametro2026());
		}
	}

	private void sembrarMatriz(Map<String, TipoContrato> tiposPorNombre,
			Map<String, Prestacion> prestacionesPorNombre) {
		List<String> laboral = List.of(
				"Salud", "Pensión", "ARL", "Caja de Compensación", "SENA", "ICBF",
				"Dotación", "Auxilio de Transporte", "Prima de Servicios",
				"Cesantías", "Intereses sobre Cesantías", "Vacaciones");
		List<String> prestacionServicios = List.of(
				"Salud (P.S.)", "Pensión (P.S.)", "ARL (P.S.)");
		List<String> aprendizaje = List.of(
				"Salud (Aprendizaje)", "ARL (Aprendizaje)");

		sembrarMatrizPara(tiposPorNombre.get("TERMINO_INDEFINIDO"), laboral, prestacionesPorNombre);
		sembrarMatrizPara(tiposPorNombre.get("TERMINO_FIJO"), laboral, prestacionesPorNombre);
		sembrarMatrizPara(tiposPorNombre.get("OBRA_LABOR"), laboral, prestacionesPorNombre);
		sembrarMatrizPara(tiposPorNombre.get("PRESTACION_SERVICIOS"), prestacionServicios, prestacionesPorNombre);
		sembrarMatrizPara(tiposPorNombre.get("APRENDIZAJE"), aprendizaje, prestacionesPorNombre);
		sembrarMatrizPara(tiposPorNombre.get("PRACTICAS_LABORALES"), aprendizaje, prestacionesPorNombre);
	}

	private void sembrarMatrizPara(TipoContrato tipo, List<String> nombresPrestaciones,
			Map<String, Prestacion> prestacionesPorNombre) {
		if (tipo == null) {
			return;
		}
		Set<Long> yaExistentes = matriz.listarPorTipoContrato(tipo.getId()).stream()
				.map(TipoContratoPrestacion::getPrestacionId)
				.collect(Collectors.toSet());
		for (String nombre : nombresPrestaciones) {
			Prestacion p = prestacionesPorNombre.get(nombre);
			if (p != null && !yaExistentes.contains(p.getId())) {
				matriz.guardar(new TipoContratoPrestacion(tipo.getId(), p.getId()));
			}
		}
	}

	private static List<TipoContrato> datosTipos() {
		return List.of(
				new TipoContrato("TERMINO_INDEFINIDO", "Contrato laboral a término indefinido", true),
				new TipoContrato("TERMINO_FIJO", "Contrato laboral a término fijo", true),
				new TipoContrato("OBRA_LABOR", "Contrato por obra o labor determinada", true),
				new TipoContrato("PRESTACION_SERVICIOS", "Contrato de prestación de servicios", true),
				new TipoContrato("APRENDIZAJE", "Contrato de aprendizaje (SENA)", true),
				new TipoContrato("PRACTICAS_LABORALES", "Prácticas laborales / pasantía", true));
	}

	private static List<Prestacion> datosPrestaciones() {
		return List.of(
				new Prestacion("Salud", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Pensión", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("ARL", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Caja de Compensación", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("SENA", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("ICBF", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Dotación", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Auxilio de Transporte", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Prima de Servicios", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Cesantías", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Intereses sobre Cesantías", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Vacaciones", Prestacion.TIPO_LABORAL, true, true),
				new Prestacion("Salud (P.S.)", Prestacion.TIPO_PRESTACION_SERVICIOS, true, true),
				new Prestacion("Pensión (P.S.)", Prestacion.TIPO_PRESTACION_SERVICIOS, true, true),
				new Prestacion("ARL (P.S.)", Prestacion.TIPO_PRESTACION_SERVICIOS, true, true),
				new Prestacion("Salud (Aprendizaje)", Prestacion.TIPO_APRENDIZAJE, true, true),
				new Prestacion("ARL (Aprendizaje)", Prestacion.TIPO_APRENDIZAJE, true, true));
	}

	private static ParametroLegal parametro2026() {
		ParametroLegal p = new ParametroLegal();
		p.setAnio(2026);
		p.setSmlmv(new BigDecimal("1520000"));
		p.setAuxilioTransporte(new BigDecimal("200000"));
		p.setPorcentajeSalud(new BigDecimal("8.5"));
		p.setPorcentajePension(new BigDecimal("12"));
		p.setPorcentajeArl(new BigDecimal("0.522"));
		p.setPorcentajeCaja(new BigDecimal("4"));
		p.setPorcentajeSena(new BigDecimal("2"));
		p.setPorcentajeIcbf(new BigDecimal("3"));
		return p;
	}
}
