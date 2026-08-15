package com.art.inventario.puerto.entrada;
public interface BackupCasoDeUso {
	byte[] crearBackup();
	void restaurar(byte[] contenido);
}
