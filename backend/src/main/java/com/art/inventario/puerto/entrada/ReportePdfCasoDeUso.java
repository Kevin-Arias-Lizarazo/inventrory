package com.art.inventario.puerto.entrada;
public interface ReportePdfCasoDeUso {
	byte[] inventario();
	byte[] facturas(String desde, String hasta);
	byte[] valorInventario();
	byte[] alertasReposicion();
}
