package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Contrato;

public interface ContratoCasoDeUso {

	List<Contrato> listar();

	PaginaResultado<Contrato> listarPagina(String q, int pagina, int tamano);

	Contrato obtener(Long id);

	Contrato crear(Contrato contrato);

	Contrato actualizar(Long id, Contrato datos);

	Contrato concluir(Long id);

	void eliminar(Long id);
}