package com.art.inventario.aplicacion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ContratoCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;

@Service
public class ContratoAplicacion implements ContratoCasoDeUso {

	private static final Set<String> ESTADOS = new HashSet<>(
			Arrays.asList(Contrato.ACTIVO, Contrato.CONCLUIDO));

	private final ContratoPersistencia persistencia;
	private final EmpleadoPersistencia empleadoPersistencia;
	private final CambiosNotificador notificador;

	public ContratoAplicacion(ContratoPersistencia persistencia, EmpleadoPersistencia empleadoPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.empleadoPersistencia = empleadoPersistencia;
		this.notificador = notificador;
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
		Contrato creado = persistencia.guardar(contrato);
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
		actual.setFechaInicio(datos.getFechaInicio());
		actual.setFechaFin(datos.getFechaFin());
		actual.setEstado(estado);
		actual.setEmpleado(datos.getEmpleado());
		Contrato guardado = persistencia.guardar(actual);
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

	private void notificar() {
		notificador.publicar(CambiosNotificador.RECURSO_CONTRATOS);
		notificador.publicar(CambiosNotificador.RECURSO_EMPLEADOS);
	}

	private void validarEstado(String estado) {
		if (!ESTADOS.contains(estado)) {
			throw new DatosInvalidosExcepcion("Estado de contrato inválido");
		}
	}
}