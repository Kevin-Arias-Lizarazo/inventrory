package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaEpp;

public interface EntregaEppCasoDeUso {

	List<EntregaEpp> listar();

	PaginaResultado<EntregaEpp> listarPagina(int pagina, int tamano);

	PaginaResultado<EntregaEpp> listarPagina(ConsultaPaginada consulta);

	PaginaResultado<EntregaEpp> listarFiltradas(String fecha, Long empleadoId, Long eppId, String orden, int pagina,
			int tamano);

	EntregaEpp obtener(Long id);

	EntregaEpp crear(EntregaEpp entrega);

	EntregaEpp actualizar(Long id, EntregaEpp datos);

	void eliminar(Long id);
}