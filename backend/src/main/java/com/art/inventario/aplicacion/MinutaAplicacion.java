package com.art.inventario.aplicacion;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Minuta;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.MinutaCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.MinutaPersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Service
public class MinutaAplicacion implements MinutaCasoDeUso {

	private final MinutaPersistencia persistencia;
	private final EmpleadoPersistencia empleadoPersistencia;
	private final ProyectoPersistencia proyectoPersistencia;
	private final ContratoPersistencia contratoPersistencia;
	private final CambiosNotificador notificador;

	public MinutaAplicacion(MinutaPersistencia persistencia, EmpleadoPersistencia empleadoPersistencia,
			ProyectoPersistencia proyectoPersistencia, ContratoPersistencia contratoPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.empleadoPersistencia = empleadoPersistencia;
		this.proyectoPersistencia = proyectoPersistencia;
		this.contratoPersistencia = contratoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Minuta> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Minuta> listarPagina(String q, int pagina, int tamano) {
		List<Minuta> lista = persistencia.listar();
		if (q != null && !q.isBlank()) {
			String criterio = q.trim().toLowerCase();
			lista = lista.stream()
					.filter(m -> (m.getProyecto() != null && m.getProyecto().getNombre() != null
							&& m.getProyecto().getNombre().toLowerCase().contains(criterio))
							|| (m.getEmpleado() != null && m.getEmpleado().getNombre() != null
									&& m.getEmpleado().getNombre().toLowerCase().contains(criterio)))
					.toList();
		}
		return PaginaResultado.deLista(lista, pagina, tamano);
	}

	@Override
	public PaginaResultado<Minuta> listarPaginaRecientes(int pagina, int tamano) {
		return persistencia.listarPaginaRecientes(PaginaResultado.paginaSegura(pagina),
				PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public PaginaResultado<Minuta> listarFiltradas(String fecha, Long empleadoId, String q, String orden, int pagina,
			int tamano) {
		List<Minuta> lista = persistencia.listar();
		if (fecha != null && !fecha.isBlank()) {
			lista = lista.stream().filter(m -> fecha.equals(m.getFecha())).toList();
		}
		if (empleadoId != null) {
			lista = lista.stream()
					.filter(m -> m.getEmpleado() != null && empleadoId.equals(m.getEmpleado().getId()))
					.toList();
		}
		if (q != null && !q.isBlank()) {
			String criterio = q.trim().toLowerCase();
			lista = lista.stream()
					.filter(m -> (m.getProyecto() != null && m.getProyecto().getNombre() != null
							&& m.getProyecto().getNombre().toLowerCase().contains(criterio))
							|| (m.getEmpleado() != null && m.getEmpleado().getNombre() != null
									&& m.getEmpleado().getNombre().toLowerCase().contains(criterio)))
					.toList();
		}
		Comparator<Minuta> comparador = Comparator.comparing(Minuta::getFecha, Comparator.nullsFirst(String::compareTo))
				.thenComparing(Minuta::getHora, Comparator.nullsFirst(String::compareTo))
				.thenComparing(Minuta::getId, Comparator.nullsFirst(Long::compareTo));
		lista = lista.stream().sorted("asc".equalsIgnoreCase(orden) ? comparador : comparador.reversed()).toList();
		return PaginaResultado.deLista(lista, pagina, tamano);
	}

	@Override
	public Minuta obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Minuta crear(Minuta minuta) {
		Long empleadoId = minuta.getEmpleado() == null ? null : minuta.getEmpleado().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		validarProyecto(minuta.getProyecto() == null ? null : minuta.getProyecto().getId());
		Minuta creada = persistencia.guardar(minuta);
		notificador.publicar(CambiosNotificador.RECURSO_MINUTAS);
		return creada;
	}

	@Override
	@Transactional
	public int crearLote(List<Minuta> minutas) {
		if (minutas == null || minutas.isEmpty()) {
			throw new DatosInvalidosExcepcion("No hay minutas para registrar");
		}
		int creadas = 0;
		for (Minuta minuta : minutas) {
			Long empleadoId = minuta.getEmpleado() == null ? null : minuta.getEmpleado().getId();
			Long proyectoId = minuta.getProyecto() == null ? null : minuta.getProyecto().getId();
			validarEmpleado(empleadoId);
			validarContratado(empleadoId);
			validarProyecto(proyectoId);
			persistencia.guardar(minuta);
			creadas++;
		}
		notificador.publicar(CambiosNotificador.RECURSO_MINUTAS);
		return creadas;
	}

	@Override
	@Transactional
	public Minuta actualizar(Long id, Minuta datos) {
		Minuta actual = persistencia.obtener(id);
		Long empleadoId = datos.getEmpleado() == null ? null : datos.getEmpleado().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		validarProyecto(datos.getProyecto() == null ? null : datos.getProyecto().getId());
		actual.setHora(datos.getHora());
		actual.setFecha(datos.getFecha());
		actual.setEmpleado(datos.getEmpleado());
		actual.setProyecto(datos.getProyecto());
		Minuta guardada = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_MINUTAS);
		return guardada;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_MINUTAS);
	}

	private void validarEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar un empleado");
		}
		try {
			empleadoPersistencia.obtener(empleadoId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Empleado no encontrado");
		}
	}

	private void validarProyecto(Long proyectoId) {
		if (proyectoId == null) {
			return;
		}
		try {
			proyectoPersistencia.obtener(proyectoId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Proyecto no encontrado");
		}
	}

	private void validarContratado(Long empleadoId) {
		if (empleadoId == null) {
			return;
		}
		if (!contratoPersistencia.empleadoContratado(empleadoId)) {
			throw new DatosInvalidosExcepcion("El empleado no tiene un contrato activo");
		}
	}
}