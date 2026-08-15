package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaRopa;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.EntregaRopaCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.EntregaRopaPersistencia;

@Service
public class EntregaRopaAplicacion implements EntregaRopaCasoDeUso {

	private final EntregaRopaPersistencia persistencia;
	private final EmpleadoPersistencia empleadoPersistencia;
	private final ContratoPersistencia contratoPersistencia;
	private final CambiosNotificador notificador;

	public EntregaRopaAplicacion(EntregaRopaPersistencia persistencia,
			EmpleadoPersistencia empleadoPersistencia, ContratoPersistencia contratoPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.empleadoPersistencia = empleadoPersistencia;
		this.contratoPersistencia = contratoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<EntregaRopa> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<EntregaRopa> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public EntregaRopa obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public EntregaRopa crear(EntregaRopa entrega) {
		Long empleadoId = entrega.getEmpleado() == null ? null : entrega.getEmpleado().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		EntregaRopa creada = persistencia.guardar(entrega);
		notificador.publicar(CambiosNotificador.RECURSO_ENTREGAS_ROPA);
		return creada;
	}

	@Override
	@Transactional
	public EntregaRopa actualizar(Long id, EntregaRopa datos) {
		EntregaRopa actual = persistencia.obtener(id);
		Long empleadoId = datos.getEmpleado() == null ? null : datos.getEmpleado().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		actual.setFecha(datos.getFecha());
		actual.setFotoUrl(datos.getFotoUrl());
		actual.setFirmaUrl(datos.getFirmaUrl());
		actual.setObservacion(datos.getObservacion());
		actual.setEmpleado(datos.getEmpleado());
		EntregaRopa guardada = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_ENTREGAS_ROPA);
		return guardada;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_ENTREGAS_ROPA);
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
}