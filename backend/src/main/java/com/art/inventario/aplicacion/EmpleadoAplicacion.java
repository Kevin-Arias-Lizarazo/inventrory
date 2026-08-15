package com.art.inventario.aplicacion;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.EmpleadoCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;

@Service
public class EmpleadoAplicacion implements EmpleadoCasoDeUso {

	private final EmpleadoPersistencia persistencia;
	private final ContratoPersistencia contratoPersistencia;
	private final CambiosNotificador notificador;

	public EmpleadoAplicacion(EmpleadoPersistencia persistencia, ContratoPersistencia contratoPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.contratoPersistencia = contratoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Empleado> listar(String q, boolean soloContratados) {
		List<Empleado> empleados = persistencia.todos();
		marcarContratados(empleados);
		if (soloContratados) {
			empleados = empleados.stream()
					.filter(e -> Boolean.TRUE.equals(e.getContratado()))
					.toList();
		}
		if (q != null && !q.isBlank()) {
			String criterio = normalizar(q);
			empleados = empleados.stream()
					.filter(e -> normalizar(e.getNombre()).contains(criterio)
							|| normalizar(e.getDocumento()).contains(criterio)
							|| normalizar(e.getCargo()).contains(criterio))
					.toList();
		}
		return empleados;
	}

	@Override
	public PaginaResultado<Empleado> listarPagina(String q, boolean soloContratados, int pagina, int tamano) {
		return PaginaResultado.deLista(listar(q, soloContratados), pagina, tamano);
	}

	@Override
	public Empleado obtener(Long id) {
		Empleado empleado = persistencia.obtener(id);
		empleado.setContratado(contratoPersistencia.empleadoContratado(id));
		return empleado;
	}

	@Override
	@Transactional
	public Empleado crear(Empleado empleado) {
		validar(empleado);
		validarNombreUnico(empleado.getNombre(), null);
		Empleado creado = persistencia.guardar(empleado);
		creado.setCodigo("E" + creado.getId());
		creado = persistencia.guardar(creado);
		notificador.publicar(CambiosNotificador.RECURSO_EMPLEADOS);
		return creado;
	}

	@Override
	@Transactional
	public Empleado actualizar(Long id, Empleado datos) {
		Empleado actual = persistencia.obtener(id);
		validar(datos);
		validarNombreUnico(datos.getNombre(), id);
		actual.setNombre(datos.getNombre());
		actual.setDocumento(datos.getDocumento());
		actual.setCargo(datos.getCargo());
		actual.setTelefono(datos.getTelefono());
		actual.setCorreo(datos.getCorreo());
		actual.setDireccion(datos.getDireccion());
		actual.setFechaIngreso(datos.getFechaIngreso());
		actual.setHojaVida(datos.getHojaVida());
		actual.setFotoUrl(datos.getFotoUrl());
		Empleado guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_EMPLEADOS);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Empleado empleado = persistencia.obtener(id);
		if (persistencia.tieneReferencias(empleado.getId()) || contratoPersistencia.tieneContratos(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el empleado tiene registros asociados");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_EMPLEADOS);
	}

	private void marcarContratados(List<Empleado> empleados) {
		Set<Long> contratados = new HashSet<>(contratoPersistencia.empleadosContratados());
		empleados.forEach(e -> e.setContratado(contratados.contains(e.getId())));
	}

	private void validar(Empleado empleado) {
		if (empleado.getNombre() == null || empleado.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe un empleado con ese nombre");
		}
	}

	private static String normalizar(String texto) {
		if (texto == null) {
			return "";
		}
		return Normalizer.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase();
	}
}