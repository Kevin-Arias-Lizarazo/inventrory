package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EventoLog;

public interface AuditoriaPersistencia {

	PaginaResultado<EventoLog> leerPagina(String fecha, String usuario, String accion, String resultado,
			Integer pagina, Integer tamano);

	List<String> fechasDisponibles();
}