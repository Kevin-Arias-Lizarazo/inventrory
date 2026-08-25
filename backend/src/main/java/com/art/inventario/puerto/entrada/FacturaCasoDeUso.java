package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Factura;

public interface FacturaCasoDeUso {

	List<Factura> listar();

	PaginaResultado<Factura> listarPagina(String q, Long proveedorId, String fecha, String estado,
			Integer pagina, Integer tamano);

	Factura obtener(Long id);

	Factura crear(Factura factura);

	Factura actualizar(Long id, Factura datos);

	void eliminar(Long id);
}