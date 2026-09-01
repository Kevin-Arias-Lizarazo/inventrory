package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.EntregaRopa;

public interface EntregaRopaCasoDeUso {

	List<EntregaRopa> listar();

	PaginaResultado<EntregaRopa> listarPagina(int pagina, int tamano);

	PaginaResultado<EntregaRopa> listarPagina(ConsultaPaginada consulta);

	EntregaRopa obtener(Long id);

	EntregaRopa crear(EntregaRopa entrega);

	EntregaRopa actualizar(Long id, EntregaRopa datos);

	void eliminar(Long id);
}