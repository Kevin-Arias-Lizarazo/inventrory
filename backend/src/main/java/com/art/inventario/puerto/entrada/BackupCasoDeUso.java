package com.art.inventario.puerto.entrada;

public interface BackupCasoDeUso {
	byte[] crearBackup();
	byte[] exportarUploads();
	void restaurar(byte[] contenido);
	void restaurarUploads(byte[] zip);
}
