package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Epp;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.EppCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.EntregaEppPersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;

@Service
public class EppAplicacion implements EppCasoDeUso {

	private final EppPersistencia persistencia;
	private final EntregaEppPersistencia entregaPersistencia;
	private final CambiosNotificador notificador;

	public EppAplicacion(EppPersistencia persistencia, EntregaEppPersistencia entregaPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.entregaPersistencia = entregaPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Epp> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Epp> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public Epp obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Epp crear(Epp epp) {
		validarNombre(epp);
		validarNombreUnico(epp.getNombre(), null);
		if (epp.getStock() == null) {
			epp.setStock(0);
		}
		Epp creado = persistencia.guardar(epp);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		return creado;
	}

	@Override
	@Transactional
	public Epp actualizar(Long id, Epp datos) {
		Epp actual = persistencia.obtener(id);
		validarNombre(datos);
		validarNombreUnico(datos.getNombre(), id);
		actual.setNombre(datos.getNombre());
		actual.setDescripcion(datos.getDescripcion());
		actual.setFotoUrl(datos.getFotoUrl());
		Epp guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (entregaPersistencia.tieneEntregasConEpp(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el EPP tiene entregas asociadas");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_EPP);
	}

	private void validarNombre(Epp epp) {
		if (epp.getNombre() == null || epp.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}

	private void validarNombreUnico(String nombre, Long excluirId) {
		if (persistencia.existeNombre(nombre, excluirId)) {
			throw new DatosInvalidosExcepcion("Ya existe un EPP con ese nombre");
		}
	}
}