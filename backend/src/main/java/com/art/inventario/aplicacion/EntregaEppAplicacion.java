package com.art.inventario.aplicacion;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.dominio.Epp;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.EntregaEppCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.EntregaEppPersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;

@Service
public class EntregaEppAplicacion implements EntregaEppCasoDeUso {

	private final EntregaEppPersistencia persistencia;
	private final EmpleadoPersistencia empleadoPersistencia;
	private final ContratoPersistencia contratoPersistencia;
	private final EppPersistencia eppPersistencia;
	private final CambiosNotificador notificador;

	public EntregaEppAplicacion(EntregaEppPersistencia persistencia,
			EmpleadoPersistencia empleadoPersistencia, ContratoPersistencia contratoPersistencia,
			EppPersistencia eppPersistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.empleadoPersistencia = empleadoPersistencia;
		this.contratoPersistencia = contratoPersistencia;
		this.eppPersistencia = eppPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<EntregaEpp> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<EntregaEpp> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public PaginaResultado<EntregaEpp> listarFiltradas(String fecha, Long empleadoId, Long eppId, String orden,
			int pagina, int tamano) {
		List<EntregaEpp> lista = persistencia.listar();
		if (fecha != null && !fecha.isBlank()) {
			lista = lista.stream().filter(m -> fecha.equals(m.getFecha())).toList();
		}
		if (empleadoId != null) {
			lista = lista.stream()
					.filter(m -> m.getEmpleado() != null && empleadoId.equals(m.getEmpleado().getId()))
					.toList();
		}
		if (eppId != null) {
			lista = lista.stream().filter(m -> m.getEpp() != null && eppId.equals(m.getEpp().getId())).toList();
		}
		Comparator<EntregaEpp> comparador = Comparator.comparing(EntregaEpp::getFecha,
				Comparator.nullsFirst(String::compareTo))
				.thenComparing(EntregaEpp::getId, Comparator.nullsFirst(Long::compareTo));
		lista = lista.stream().sorted("asc".equalsIgnoreCase(orden) ? comparador : comparador.reversed()).toList();
		return PaginaResultado.deLista(lista, pagina, tamano);
	}

	@Override
	public EntregaEpp obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public EntregaEpp crear(EntregaEpp entrega) {
		Long empleadoId = entrega.getEmpleado() == null ? null : entrega.getEmpleado().getId();
		Long eppId = entrega.getEpp() == null ? null : entrega.getEpp().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		Epp epp = resolverEpp(eppId);
		if (stock(epp) <= 0) {
			throw new DatosInvalidosExcepcion("No hay unidades disponibles de este EPP");
		}
		epp.setStock(stock(epp) - 1);
		eppPersistencia.guardar(epp);
		entrega.setEpp(epp);
		EntregaEpp creada = persistencia.guardar(entrega);
		notificar();
		return creada;
	}

	@Override
	@Transactional
	public EntregaEpp actualizar(Long id, EntregaEpp datos) {
		EntregaEpp actual = persistencia.obtener(id);
		Long empleadoId = datos.getEmpleado() == null ? null : datos.getEmpleado().getId();
		Long eppId = datos.getEpp() == null ? null : datos.getEpp().getId();
		validarEmpleado(empleadoId);
		validarContratado(empleadoId);
		Epp eppNuevo = resolverEpp(eppId);
		Epp eppAnterior = resolverEpp(actual.getEpp() == null ? null : actual.getEpp().getId());
		if (!Objects.equals(eppAnterior.getId(), eppNuevo.getId())) {
			eppAnterior.setStock(stock(eppAnterior) + 1);
			eppPersistencia.guardar(eppAnterior);
			if (stock(eppNuevo) <= 0) {
				throw new DatosInvalidosExcepcion("No hay unidades disponibles de este EPP");
			}
			eppNuevo.setStock(stock(eppNuevo) - 1);
			eppPersistencia.guardar(eppNuevo);
		}
		actual.setFecha(datos.getFecha());
		actual.setObservacion(datos.getObservacion());
		actual.setFotoUrl(datos.getFotoUrl());
		actual.setFirmaUrl(datos.getFirmaUrl());
		actual.setEmpleado(datos.getEmpleado());
		actual.setEpp(eppNuevo);
		EntregaEpp guardada = persistencia.guardar(actual);
		notificar();
		return guardada;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		EntregaEpp actual = persistencia.obtener(id);
		Epp epp = resolverEpp(actual.getEpp() == null ? null : actual.getEpp().getId());
		epp.setStock(stock(epp) + 1);
		eppPersistencia.guardar(epp);
		persistencia.eliminar(id);
		notificar();
	}

	private void notificar() {
		notificador.publicar(CambiosNotificador.RECURSO_ENTREGAS_EPP);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
	}

	private Epp resolverEpp(Long eppId) {
		if (eppId == null) {
			throw new DatosInvalidosExcepcion("Debe seleccionar un EPP del inventario");
		}
		try {
			return eppPersistencia.obtener(eppId);
		} catch (RuntimeException e) {
			throw new DatosInvalidosExcepcion("EPP no encontrado en el inventario");
		}
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

	private void validarContratado(Long empleadoId) {
		if (!contratoPersistencia.empleadoContratado(empleadoId)) {
			throw new DatosInvalidosExcepcion("El empleado no tiene un contrato activo");
		}
	}

	private static int stock(Epp epp) {
		return epp.getStock() == null ? 0 : epp.getStock();
	}
}