package com.art.inventario.puerto.entrada;
import com.art.inventario.dominio.ResumenDashboard;
public interface DashboardCasoDeUso {
	ResumenDashboard resumen(String desde, String hasta);
}
