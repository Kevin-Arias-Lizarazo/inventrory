package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Compra;

public interface CompraCasoDeUso {

	List<Compra> listar();

	PaginaResultado<Compra> listarPagina(String q, Long proveedorId, String fecha, Boolean facturada,
			Integer pagina, Integer tamano);

	Compra obtener(Long id);

	Compra crear(Compra compra);

	Compra actualizar(Long id, Compra datos);

	void eliminar(Long id);
}