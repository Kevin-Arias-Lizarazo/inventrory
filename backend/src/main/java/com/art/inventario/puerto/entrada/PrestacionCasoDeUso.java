package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Prestacion;

public interface PrestacionCasoDeUso {

	List<Prestacion> listar();

	PaginaResultado<Prestacion> listarPagina(int pagina, int tamano);

	Prestacion obtener(Long id);

	Prestacion crear(Prestacion prestacion);

	Prestacion actualizar(Long id, Prestacion datos);

	void eliminar(Long id);

	List<Prestacion> listarPorTipoContrato(Long tipoContratoId);
}
