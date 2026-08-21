package com.art.inventario.puerto.salida;

import com.art.inventario.dominio.EventoLog;

public interface RegistroAuditoria {

	void registrar(EventoLog evento);
}