package com.art.inventario.aplicacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.aplicacion.dto.PrestacionesContrato;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.dominio.ContratoPrestacionExtra;
import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ContratoCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPrestacionCalculadaPersistencia;
import com.art.inventario.puerto.salida.ContratoPrestacionExtraPersistencia;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;

@Service
public class ContratoAplicacion implements ContratoCasoDeUso {

	private static final Set<String> ESTADOS = new HashSet<>(
			Arrays.asList(Contrato.ACTIVO, Contrato.CONCLUIDO));
	private static final Set<String> TIPOS_EXTRA = new HashSet<>(
			Arrays.asList(ContratoPrestacionExtra.TIPO_RECURRENTE, ContratoPrestacionExtra.TIPO_EVENTUAL));

	private final ContratoPersistencia persistencia;
	private final EmpleadoPersistencia empleadoPersistencia;
	private final CambiosNotificador notificador;
	private final CalculadoraPrestaciones calculadora;
	private final ContratoPrestacionCalculadaPersistencia calculadasPersistencia;
	private final ContratoPrestacionExtraPersistencia extrasPersistencia;
	private final ParametroLegalPersistencia parametroLegalPersistencia;

	public ContratoAplicacion(ContratoPersistencia persistencia, EmpleadoPersistencia empleadoPersistencia,
			CambiosNotificador notificador, CalculadoraPrestaciones calculadora,
			ContratoPrestacionCalculadaPersistencia calculadasPersistencia,
			ContratoPrestacionExtraPersistencia extrasPersistencia,
			ParametroLegalPersistencia parametroLegalPersistencia) {
		this.persistencia = persistencia;
		this.empleadoPersistencia = empleadoPersistencia;
		this.notificador = notificador;
		this.calculadora = calculadora;
		this.calculadasPersistencia = calculadasPersistencia;
		this.extrasPersistencia = extrasPersistencia;
		this.parametroLegalPersistencia = parametroLegalPersistencia;
	}

	@Override
	public List<Contrato> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Contrato> listarPagina(String q, int pagina, int tamano) {
		List<Contrato> lista = persistencia.listar();
		if (q != null && !q.isBlank()) {
			String criterio = q.trim().toLowerCase();
			lista = lista.stream()
					.filter(c -> c.getEmpleado() != null && c.getEmpleado().getNombre() != null
							&& c.getEmpleado().getNombre().toLowerCase().contains(criterio))
					.toList();
		}
		return PaginaResultado.deLista(lista, pagina, tamano);
	}

	@Override
	public Contrato obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Contrato crear(Contrato contrato) {
		Long empleadoId = contrato.getEmpleado() == null ? null : contrato.getEmpleado().getId();
		if (empleadoId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar un empleado");
		}
		try {
			empleadoPersistencia.obtener(empleadoId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Empleado no encontrado");
		}
		if (contrato.getEstado() == null || contrato.getEstado().isBlank()) {
			contrato.setEstado(Contrato.ACTIVO);
		}
		validarEstado(contrato.getEstado());
		if (contrato.getFechaInicio() == null || contrato.getFechaInicio().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha de inicio es obligatoria");
		}
		validarDatosContrato(contrato);
		Contrato creado = persistencia.guardar(contrato);
		recalcularSiAplica(creado);
		notificar();
		return creado;
	}

	@Override
	@Transactional
	public Contrato actualizar(Long id, Contrato datos) {
		Contrato actual = persistencia.obtener(id);
		Long empleadoId = datos.getEmpleado() == null ? null : datos.getEmpleado().getId();
		if (empleadoId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar un empleado");
		}
		try {
			empleadoPersistencia.obtener(empleadoId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Empleado no encontrado");
		}
		String estado = datos.getEstado() == null || datos.getEstado().isBlank()
				? Contrato.ACTIVO
				: datos.getEstado();
		validarEstado(estado);
		validarDatosContrato(datos);
		actual.setFechaInicio(datos.getFechaInicio());
		actual.setFechaFin(datos.getFechaFin());
		actual.setEstado(estado);
		actual.setEmpleado(datos.getEmpleado());
		actual.setTipoContrato(datos.getTipoContrato());
		actual.setRemuneracionMensual(datos.getRemuneracionMensual());
		actual.setFaseAprendizaje(datos.getFaseAprendizaje());
		Contrato guardado = persistencia.guardar(actual);
		recalcularSiAplica(guardado);
		notificar();
		return guardado;
	}

	@Override
	@Transactional
	public Contrato concluir(Long id) {
		Contrato actual = persistencia.obtener(id);
		actual.setEstado(Contrato.CONCLUIDO);
		Contrato guardado = persistencia.guardar(actual);
		notificar();
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.eliminar(id);
		notificar();
	}

	@Override
	@Transactional
	public List<ContratoPrestacionCalculada> calcularPrestaciones(Long id) {
		Contrato contrato = persistencia.obtener(id);
		TipoContrato tipo = contrato.getTipoContrato();
		if (tipo == null) {
			throw new DatosInvalidosExcepcion(
					"El contrato debe tener un tipo de contrato para calcular prestaciones");
		}
		if (!calculadora.aplicaCalculo(tipo)) {
			throw new DatosInvalidosExcepcion("No se pueden calcular prestaciones para este tipo de contrato");
		}
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo,
				contrato.getRemuneracionMensual(), contrato.getFaseAprendizaje(), parametroVigente());
		reemplazarSnapshot(id, lineas);
		notificar();
		return listarPrestaciones(id).getCalculadas();
	}

	@Override
	public PrestacionesContrato listarPrestaciones(Long id) {
		persistencia.obtener(id);
		List<ContratoPrestacionCalculada> calculadas = calculadasPersistencia.listarPorContrato(id);
		List<ContratoPrestacionExtra> extras = extrasPersistencia.listarPorContrato(id);
		BigDecimal totalEmpleador = totalEmpleador(calculadas, extras);
		return new PrestacionesContrato(calculadas, extras, totalEmpleador);
	}

	@Override
	@Transactional
	public ContratoPrestacionExtra agregarExtra(Long id, ContratoPrestacionExtra extra) {
		persistencia.obtener(id);
		validarExtra(extra);
		extra.setId(null);
		extra.setContratoId(id);
		ContratoPrestacionExtra guardado = extrasPersistencia.guardar(extra);
		notificar();
		return guardado;
	}

	@Override
	@Transactional
	public void eliminarExtra(Long id, Long extraId) {
		persistencia.obtener(id);
		ContratoPrestacionExtra extra = extrasPersistencia.obtener(extraId);
		if (!Long.valueOf(id).equals(extra.getContratoId())) {
			throw new DatosInvalidosExcepcion("La prestación extra no pertenece al contrato");
		}
		extrasPersistencia.eliminar(extraId);
		notificar();
	}

	private void recalcularSiAplica(Contrato contrato) {
		TipoContrato tipo = contrato.getTipoContrato();
		if (tipo == null || !calculadora.aplicaCalculo(tipo)) {
			// Sin tipo (o tipo sin cálculo): el contrato ya no genera prestaciones.
			// Limpia el snapshot previo para no dejar líneas huérfanas que no
			// corresponden al estado actual del contrato.
			if (contrato.getId() != null) {
				calculadasPersistencia.eliminarPorContrato(contrato.getId());
			}
			return;
		}
		List<ContratoPrestacionCalculada> lineas = calculadora.calcular(tipo,
				contrato.getRemuneracionMensual(), contrato.getFaseAprendizaje(), parametroVigente());
		reemplazarSnapshot(contrato.getId(), lineas);
	}

	private void reemplazarSnapshot(Long contratoId, List<ContratoPrestacionCalculada> lineas) {
		calculadasPersistencia.eliminarPorContrato(contratoId);
		String fecha = LocalDate.now().toString();
		for (ContratoPrestacionCalculada linea : lineas) {
			linea.setId(null);
			linea.setContratoId(contratoId);
			linea.setFechaCalculo(fecha);
			calculadasPersistencia.guardar(linea);
		}
	}

	private ParametroLegal parametroVigente() {
		Optional<ParametroLegal> deEsteAnio = parametroLegalPersistencia.porAnio(anoCorriente());
		if (deEsteAnio.isPresent()) {
			return deEsteAnio.get();
		}
		List<ParametroLegal> todos = parametroLegalPersistencia.listar();
		if (todos.isEmpty()) {
			throw new DatosInvalidosExcepcion(
					"No hay parámetros legales configurados para calcular prestaciones");
		}
		return todos.stream()
				.max((a, b) -> Integer.compare(a.getAnio(), b.getAnio()))
				.orElseThrow();
	}

	private int anoCorriente() {
		return LocalDate.now().getYear();
	}

	private BigDecimal totalEmpleador(List<ContratoPrestacionCalculada> calculadas,
			List<ContratoPrestacionExtra> extras) {
		BigDecimal total = BigDecimal.ZERO;
		for (ContratoPrestacionCalculada c : calculadas) {
			if (CalculadoraPrestaciones.QUIEN_PAGA_EMPLEADOR.equals(c.getQuienPaga())
					&& c.getValorMensual() != null) {
				total = total.add(c.getValorMensual());
			}
		}
		for (ContratoPrestacionExtra x : extras) {
			if (x.getValor() != null) {
				total = total.add(x.getValor());
			}
		}
		return total;
	}

	private void validarExtra(ContratoPrestacionExtra extra) {
		if (extra.getConcepto() == null || extra.getConcepto().isBlank()) {
			throw new DatosInvalidosExcepcion("El concepto de la prestación extra es obligatorio");
		}
		if (extra.getTipo() == null || !TIPOS_EXTRA.contains(extra.getTipo())) {
			throw new DatosInvalidosExcepcion("El tipo de prestación extra debe ser RECURRENTE o EVENTUAL");
		}
		if (extra.getValor() == null || extra.getValor().signum() < 0) {
			throw new DatosInvalidosExcepcion("El valor de la prestación extra es obligatorio y no puede ser negativo");
		}
	}

	private void notificar() {
		notificador.publicar(CambiosNotificador.RECURSO_CONTRATOS);
		notificador.publicar(CambiosNotificador.RECURSO_EMPLEADOS);
	}

	private void validarEstado(String estado) {
		if (!ESTADOS.contains(estado)) {
			throw new DatosInvalidosExcepcion("Estado de contrato inválido");
		}
	}

	private void validarDatosContrato(Contrato contrato) {
		if (contrato.getRemuneracionMensual() != null && contrato.getRemuneracionMensual().signum() < 0) {
			throw new DatosInvalidosExcepcion("La remuneración mensual no puede ser negativa");
		}
		String fase = contrato.getFaseAprendizaje();
		if (fase != null && !fase.isBlank()
				&& !Contrato.FASE_LECTIVA.equals(fase) && !Contrato.FASE_PRACTICA.equals(fase)) {
			throw new DatosInvalidosExcepcion("La fase de aprendizaje debe ser LECTIVA o PRACTICA");
		}
	}
}
