package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.aplicacion.dto.PrestacionesContrato;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.ContratoPrestacionExtra;

public interface ContratoCasoDeUso {

	List<Contrato> listar();

	PaginaResultado<Contrato> listarPagina(String q, int pagina, int tamano);

	Contrato obtener(Long id);

	Contrato crear(Contrato contrato);

	Contrato actualizar(Long id, Contrato datos);

	Contrato concluir(Long id);

	void eliminar(Long id);

	List<com.art.inventario.dominio.ContratoPrestacionCalculada> calcularPrestaciones(Long id);

	PrestacionesContrato listarPrestaciones(Long id);

	ContratoPrestacionExtra agregarExtra(Long id, ContratoPrestacionExtra extra);

	void eliminarExtra(Long id, Long extraId);
}