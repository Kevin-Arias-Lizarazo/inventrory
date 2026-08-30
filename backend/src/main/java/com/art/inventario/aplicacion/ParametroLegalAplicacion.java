package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ParametroLegalCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;

@Service
public class ParametroLegalAplicacion implements ParametroLegalCasoDeUso {

	private final ParametroLegalPersistencia persistencia;
	private final CambiosNotificador notificador;

	public ParametroLegalAplicacion(ParametroLegalPersistencia persistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.notificador = notificador;
	}

	@Override
	public List<ParametroLegal> listar() {
		return persistencia.listar();
	}

	@Override
	public ParametroLegal obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public ParametroLegal crear(ParametroLegal parametro) {
		validar(parametro);
		if (persistencia.existeAnio(parametro.getAnio())) {
			throw new DatosInvalidosExcepcion("Ya existe un parámetro legal para ese año");
		}
		ParametroLegal creado = persistencia.guardar(parametro);
		notificador.publicar(CambiosNotificador.RECURSO_PARAMETROS_LEGALES);
		return creado;
	}

	@Override
	@Transactional
	public ParametroLegal actualizar(Long id, ParametroLegal datos) {
		ParametroLegal actual = persistencia.obtener(id);
		validar(datos);
		actual.setAnio(datos.getAnio());
		actual.setSmlmv(datos.getSmlmv());
		actual.setAuxilioTransporte(datos.getAuxilioTransporte());
		actual.setPorcentajeSalud(datos.getPorcentajeSalud());
		actual.setPorcentajePension(datos.getPorcentajePension());
		actual.setPorcentajeArl(datos.getPorcentajeArl());
		actual.setPorcentajeCaja(datos.getPorcentajeCaja());
		actual.setPorcentajeSena(datos.getPorcentajeSena());
		actual.setPorcentajeIcbf(datos.getPorcentajeIcbf());
		ParametroLegal guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_PARAMETROS_LEGALES);
		return guardado;
	}

	private void validar(ParametroLegal parametro) {
		if (parametro.getAnio() <= 0) {
			throw new DatosInvalidosExcepcion("El año es obligatorio");
		}
		if (parametro.getSmlmv() == null || parametro.getSmlmv().signum() <= 0) {
			throw new DatosInvalidosExcepcion("El SMLMV es obligatorio y debe ser mayor a cero");
		}
	}
}
