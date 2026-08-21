package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.EventoLog;

public interface AuditoriaPersistencia {

	List<EventoLog> leer(String fecha, String usuario, String accion, String resultado);

	List<String> fechasDisponibles();
}