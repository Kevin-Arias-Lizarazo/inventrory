package com.art.inventario.puerto.salida;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface CambiosNotificador {

	String RECURSO_EMPLEADOS = "empleados";
	String RECURSO_CONTRATOS = "contratos";
	String RECURSO_MINUTAS = "minutas";
	String RECURSO_PROYECTOS = "proyectos";
	String RECURSO_ENTREGAS_ROPA = "entregas-ropa";
	String RECURSO_ENTREGAS_EPP = "entregas-epp";
	String RECURSO_EPP = "epp";
	String RECURSO_ASIGNACIONES = "asignaciones-herramientas";
	String RECURSO_HERRAMIENTAS = "herramientas";
	String RECURSO_MOVIMIENTOS_HERRAMIENTAS = "movimientos-herramientas";
	String RECURSO_MATERIALES = "materiales";
	String RECURSO_CONSUMIBLES = "consumibles";
	String RECURSO_MOVIMIENTOS_MATERIALES = "movimientos-materiales";
	String RECURSO_MOVIMIENTOS_CONSUMIBLES = "movimientos-consumibles";
	String RECURSO_ASIGNACIONES_CONSUMIBLES = "asignaciones-consumibles";
	String RECURSO_MOVIMIENTOS_EPP = "movimientos-epp";
	String RECURSO_PROVEEDORES = "proveedores";
	String RECURSO_COMPRAS = "compras";
	String RECURSO_FACTURAS = "facturas";
	String RECURSO_AJUSTES = "ajustes";
	String RECURSO_DEVOLUCIONES = "devoluciones";
	String RECURSO_PAGOS_FACTURA = "pagos-factura";
	String RECURSO_ORDENES_COMPRA = "ordenes-compra";

	SseEmitter suscribir();

	void publicar(String recurso);
}
