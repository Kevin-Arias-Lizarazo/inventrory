package com.art.inventario.aplicacion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.PrestacionCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.PrestacionPersistencia;
import com.art.inventario.puerto.salida.TipoContratoPersistencia;

@Service
public class PrestacionAplicacion implements PrestacionCasoDeUso {

	private static final Set<String> TIPOS = new HashSet<>(
			Arrays.asList(Prestacion.TIPO_LABORAL, Prestacion.TIPO_PRESTACION_SERVICIOS, Prestacion.TIPO_APRENDIZAJE));

	private final PrestacionPersistencia persistencia;
	private final TipoContratoPersistencia tipoContratoPersistencia;
	private final CambiosNotificador notificador;

	public PrestacionAplicacion(PrestacionPersistencia persistencia, TipoContratoPersistencia tipoContratoPersistencia,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.tipoContratoPersistencia = tipoContratoPersistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Prestacion> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Prestacion> listarPagina(int pagina, int tamano) {
		return PaginaResultado.deLista(persistencia.listar(), pagina, tamano);
	}

	@Override
	public PaginaResultado<Prestacion> listarPagina(ConsultaPaginada consulta) {
		return persistencia.listarPagina(consulta);
	}

	@Override
	public Prestacion obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	public List<Prestacion> listarPorTipoContrato(Long tipoContratoId) {
		tipoContratoPersistencia.obtener(tipoContratoId);
		return persistencia.listarPorTipoContrato(tipoContratoId);
	}

	@Override
	@Transactional
	public Prestacion crear(Prestacion prestacion) {
		validar(prestacion);
		if (persistencia.existeNombre(prestacion.getNombre(), null)) {
			throw new DatosInvalidosExcepcion("Ya existe una prestación con ese nombre");
		}
		Prestacion creado = persistencia.guardar(prestacion);
		notificador.publicar(CambiosNotificador.RECURSO_PRESTACIONES);
		return creado;
	}

	@Override
	@Transactional
	public Prestacion actualizar(Long id, Prestacion datos) {
		Prestacion actual = persistencia.obtener(id);
		validar(datos);
		if (persistencia.existeNombre(datos.getNombre(), id)) {
			throw new DatosInvalidosExcepcion("Ya existe una prestación con ese nombre");
		}
		actual.setNombre(datos.getNombre());
		actual.setTipo(datos.getTipo());
		actual.setObligatoria(datos.isObligatoria());
		actual.setActivo(datos.isActivo());
		Prestacion guardado = persistencia.guardar(actual);
		notificador.publicar(CambiosNotificador.RECURSO_PRESTACIONES);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_PRESTACIONES);
	}

	private void validar(Prestacion prestacion) {
		if (prestacion.getNombre() == null || prestacion.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
		if (prestacion.getTipo() == null || !TIPOS.contains(prestacion.getTipo())) {
			throw new DatosInvalidosExcepcion("Tipo de prestación inválido");
		}
	}
}
