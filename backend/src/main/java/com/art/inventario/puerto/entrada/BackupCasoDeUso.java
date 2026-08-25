package com.art.inventario.puerto.entrada;

public interface BackupCasoDeUso {
	byte[] crearBackup();
	byte[] exportarCompleto();
	void restaurar(byte[] contenido);
	void restaurarUploads(byte[] zip);
}
