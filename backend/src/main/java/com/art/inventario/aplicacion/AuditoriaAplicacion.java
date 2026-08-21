package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.puerto.entrada.AuditoriaCasoDeUso;
import com.art.inventario.puerto.salida.AuditoriaPersistencia;

@Service
public class AuditoriaAplicacion implements AuditoriaCasoDeUso {

	private final AuditoriaPersistencia persistencia;

	public AuditoriaAplicacion(AuditoriaPersistencia persistencia) {
		this.persistencia = persistencia;
	}

	@Override
	public PaginaResultado<EventoLog> consultar(String fecha, String usuario, String accion, String resultado,
			Integer pagina, Integer tamano) {
		List<EventoLog> eventos = persistencia.leer(fecha, usuario, accion, resultado);
		return PaginaResultado.deLista(eventos, pagina, tamano);
	}

	@Override
	public List<String> fechasDisponibles() {
		return persistencia.fechasDisponibles();
	}
}