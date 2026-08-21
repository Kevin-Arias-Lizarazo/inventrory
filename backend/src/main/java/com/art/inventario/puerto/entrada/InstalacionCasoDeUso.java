package com.art.inventario.puerto.entrada;

import com.art.inventario.aplicacion.dto.RespuestaInstalacion;

public interface InstalacionCasoDeUso {

	boolean pendiente();

	RespuestaInstalacion completar(String rootPassword, byte[] dbArchivo, byte[] uploadsZip);
}