package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.AsignacionHerramientaCasoDeUso;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;

@Service
public class AsignacionHerramientaAplicacion implements AsignacionHerramientaCasoDeUso {

	private final AsignacionHerramientaPersistencia persistencia;
	private final EmpleadoPersistencia empleadoPersistencia;
	private final HerramientaPersistencia herramientaPersistencia;
	private final ContratoPersistencia contratoPersistencia;
	private final CambiosNotificador notificador;

	public AsignacionHerramientaAplicacion(AsignacionHerramientaPersistencia persistencia,
			EmpleadoPersistencia empleadoPersistencia, HerramientaPersistencia herramientaPersistencia,
			ContratoPersistencia contratoPersistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.empleadoPersistencia = empleadoPersistencia;
		this.herramientaPersistencia = herramientaPersistencia;
		this.contratoPersistencia = contratoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<AsignacionHerramienta> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<AsignacionHerramienta> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public AsignacionHerramienta obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public AsignacionHerramienta crear(AsignacionHerramienta asignacion) {
		Long empleadoId = asignacion.getEmpleado() == null ? null : asignacion.getEmpleado().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		Long herramientaId = asignacion.getHerramienta() == null ? null : asignacion.getHerramienta().getId();
		validarDisponible(herramientaId, null);
		AsignacionHerramienta creada = persistencia.guardar(asignacion);
		notificar();
		return creada;
	}

	@Override
	@Transactional
	public AsignacionHerramienta actualizar(Long id, AsignacionHerramienta datos) {
		AsignacionHerramienta actual = persistencia.obtener(id);
		Long empleadoId = datos.getEmpleado() == null ? null : datos.getEmpleado().getId();
		validarEmpleado(empleadoId);
		boolean devolucion = Boolean.TRUE.equals(datos.getDevuelta());
		if (!devolucion) {
			validarContratado(empleadoId);
		}
		Long herramientaId = datos.getHerramienta() == null ? null : datos.getHerramienta().getId();
		validarDisponible(herramientaId, id);
		actual.setLugar(datos.getLugar());
		actual.setFecha(datos.getFecha());
		actual.setDevuelta(datos.getDevuelta());
		actual.setFechaDevolucion(datos.getFechaDevolucion());
		actual.setEmpleado(datos.getEmpleado());
		actual.setHerramienta(datos.getHerramienta());
		AsignacionHerramienta guardada = persistencia.guardar(actual);
		notificar();
		return guardada;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.eliminar(id);
		notificar();
	}

	private void notificar() {
		notificador.publicar(CambiosNotificador.RECURSO_ASIGNACIONES);
		notificador.publicar(CambiosNotificador.RECURSO_HERRAMIENTAS);
	}

	private void validarEmpleado(Long empleadoId) {
		if (empleadoId == null) {
			return;
		}
		try {
			empleadoPersistencia.obtener(empleadoId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Empleado no encontrado");
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

	private void validarHerramienta(Long herramientaId) {
		if (herramientaId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar una herramienta del inventario");
		}
		try {
			herramientaPersistencia.obtener(herramientaId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("Herramienta no encontrada en el inventario");
		}
	}

	private void validarDisponible(Long herramientaId, Long excluirId) {
		validarHerramienta(herramientaId);
		Herramienta herramienta = herramientaPersistencia.obtener(herramientaId);
		long asignada = persistencia.contarAsignacionesActivas(herramientaId, excluirId);
		int total = herramienta.getCantidadTotal() == null ? 0 : herramienta.getCantidadTotal();
		int danada = herramienta.getCantidadDanada() == null ? 0 : herramienta.getCantidadDanada();
		int perdida = herramienta.getCantidadPerdida() == null ? 0 : herramienta.getCantidadPerdida();
		if (total - (int) asignada - danada - perdida <= 0) {
			throw new DatosInvalidosExcepcion("No hay unidades disponibles de esta herramienta");
		}
	}
}