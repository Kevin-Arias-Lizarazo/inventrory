package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaRopa;

public interface EntregaRopaPersistencia {

	List<EntregaRopa> listar();

	PaginaResultado<EntregaRopa> listarPagina(int pagina, int tamano);

	PaginaResultado<EntregaRopa> listarPagina(ConsultaPaginada consultaPaginada);

	EntregaRopa obtener(Long id);

	EntregaRopa guardar(EntregaRopa entrega);

	void eliminar(Long id);
}