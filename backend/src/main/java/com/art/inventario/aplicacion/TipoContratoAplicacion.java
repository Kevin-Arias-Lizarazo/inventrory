package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.TipoContratoCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPrestacionPersistencia;

@Service
public class TipoContratoAplicacion implements TipoContratoCasoDeUso {

	private final TipoContratoPersistencia persistencia;
	private final TipoContratoPrestacionPersistencia matriz;
	private final CambiosNotificador notificador;

	public TipoContratoAplicacion(TipoContratoPersistencia persistencia, TipoContratoPrestacionPersistencia matriz,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.matriz = matriz;
		this.notificador = notificador;
	}

	@Override
	public List<TipoContrato> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<TipoContrato> listarPagina(ConsultaPaginada consulta) {
		return persistencia.listarPagina(consulta);
	}

	@Override
	public TipoContrato obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public TipoContrato crear(TipoContrato tipoContrato) {
		validarNombre(tipoContrato);
		if (persistencia.existeNombre(tipoContrato.getNombre(), null)) {
			throw new DatosInvalidosExcepcion("Ya existe un tipo de contrato con ese nombre");
		}
		if (tipoContrato.getDescripcion() == null) {
			tipoContrato.setDescripcion("");
		}
		TipoContrato creado = persistencia.guardar(tipoContrato);
		notificador.publicar(CambiosNotificador.RECURSO_TIPOS_CONTRATO);
		return creado;
	}

	@Override
	@Transactional
	public TipoContrato actualizar(Long id, TipoContrato datos) {
		TipoContrato actual = persistencia.obtener(id);
		validarNombre(datos);
		if (persistencia.existeNombre(datos.getNombre(), id)) {
			throw new DatosInvalidosExcepcion("Ya existe un tipo de contrato con ese nombre");
		}
		actual.setNombre(datos.getNombre());
		actual.setDescripcion(datos.getDescripcion());
		actual.setActivo(datos.isActivo());
		TipoContrato guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_TIPOS_CONTRATO);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (!matriz.listarPorTipoContrato(id).isEmpty()) {
			throw new ConflictoExcepcion("No se puede eliminar: el tipo de contrato tiene prestaciones asociadas");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_TIPOS_CONTRATO);
	}

	private void validarNombre(TipoContrato tipoContrato) {
		if (tipoContrato.getNombre() == null || tipoContrato.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}
}
