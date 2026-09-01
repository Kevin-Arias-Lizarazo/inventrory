package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Minuta;

public interface MinutaCasoDeUso {

	List<Minuta> listar();

	PaginaResultado<Minuta> listarPagina(ConsultaPaginada consulta);

	PaginaResultado<Minuta> listarPaginaRecientes(int pagina, int tamano);

	PaginaResultado<Minuta> listarFiltradas(String fecha, Long empleadoId, String q, String orden, int pagina, int tamano);

	Minuta obtener(Long id);

	Minuta crear(Minuta minuta);

	int crearLote(List<Minuta> minutas);

	Minuta actualizar(Long id, Minuta datos);

	void eliminar(Long id);
}