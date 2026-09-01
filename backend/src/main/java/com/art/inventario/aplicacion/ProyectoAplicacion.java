package com.art.inventario.aplicacion;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ProyectoCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.MinutaPersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Service
public class ProyectoAplicacion implements ProyectoCasoDeUso {

	private static final Set<String> ESTADOS = new HashSet<>(
			Arrays.asList(Proyecto.ACTIVO, Proyecto.FINALIZADO));

	private final ProyectoPersistencia persistencia;
	private final MinutaPersistencia minutaPersistencia;
	private final CambiosNotificador notificador;

	public ProyectoAplicacion(ProyectoPersistencia persistencia, MinutaPersistencia minutaPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.minutaPersistencia = minutaPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Proyecto> listar(String estado) {
		List<Proyecto> lista = persistencia.listar();
		if (estado != null && !estado.isBlank()) {
			lista = lista.stream().filter(p -> estado.equals(p.getEstado())).toList();
		}
		return lista;
	}

	@Override
	public PaginaResultado<Proyecto> listarPagina(ConsultaPaginada consulta) {
		return persistencia.listarPagina(consulta);
	}

	@Override
	public Proyecto obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Proyecto crear(Proyecto proyecto) {
		validarNombre(proyecto);
		validarNombreUnico(proyecto.getNombre(), null);
		if (proyecto.getEstado() == null || proyecto.getEstado().isBlank()) {
			proyecto.setEstado(Proyecto.ACTIVO);
		}
		validarEstado(proyecto.getEstado());
		Proyecto creado = persistencia.guardar(proyecto);
		creado.setCodigo("P" + creado.getId());
		creado = persistencia.guardar(creado);
		notificador.publicar(CambiosNotificador.RECURSO_PROYECTOS);
		return creado;
	}

	@Override
	@Transactional
	public Proyecto actualizar(Long id, Proyecto datos) {
		Proyecto actual = persistencia.obtener(id);
		validarNombre(datos);
		validarNombreUnico(datos.getNombre(), id);
		String estado = datos.getEstado() == null || datos.getEstado().isBlank()
				? Proyecto.ACTIVO
				: datos.getEstado();
		validarEstado(estado);
		actual.setNombre(datos.getNombre());
		actual.setCliente(datos.getCliente());
		actual.setUbicacion(datos.getUbicacion());
		actual.setDescripcion(datos.getDescripcion());
		actual.setFechaInicio(datos.getFechaInicio());
		actual.setFechaFin(datos.getFechaFin());
		actual.setEstado(estado);
		Proyecto guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_PROYECTOS);
		return guardado;
	}

	@Override
	@Transactional
	public Proyecto finalizar(Long id) {
		Proyecto actual = persistencia.obtener(id);
		actual.setEstado(Proyecto.FINALIZADO);
		if (actual.getFechaFin() == null || actual.getFechaFin().isBlank()) {
			actual.setFechaFin(LocalDate.now().toString());
		}
		Proyecto guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_PROYECTOS);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (minutaPersistencia.tieneMinutasConProyecto(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el proyecto tiene minutas asociadas");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_PROYECTOS);
	}

	private void validarNombre(Proyecto proyecto) {
		if (proyecto.getNombre() == null || proyecto.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe un proyecto con ese nombre");
		}
	}

	private void validarEstado(String estado) {
		if (!ESTADOS.contains(estado)) {
			throw new DatosInvalidosExcepcion("Estado de proyecto inválido");
		}
	}
}