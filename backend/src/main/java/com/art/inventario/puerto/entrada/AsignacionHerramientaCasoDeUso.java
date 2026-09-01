package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionHerramienta;

public interface AsignacionHerramientaCasoDeUso {

	List<AsignacionHerramienta> listar();

	PaginaResultado<AsignacionHerramienta> listarPagina(int pagina, int tamano);

	PaginaResultado<AsignacionHerramienta> listarPagina(ConsultaPaginada consulta);

	AsignacionHerramienta obtener(Long id);

	AsignacionHerramienta crear(AsignacionHerramienta asignacion);

	AsignacionHerramienta actualizar(Long id, AsignacionHerramienta datos);

	void eliminar(Long id);
}