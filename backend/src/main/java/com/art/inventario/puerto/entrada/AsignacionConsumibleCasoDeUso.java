package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionConsumible;

public interface AsignacionConsumibleCasoDeUso {

	List<AsignacionConsumible> listar();

	PaginaResultado<AsignacionConsumible> listarPagina(int pagina, int tamano);

	PaginaResultado<AsignacionConsumible> listarPagina(ConsultaPaginada consulta);

	AsignacionConsumible obtener(Long id);

	AsignacionConsumible crear(AsignacionConsumible asignacion);

	AsignacionConsumible actualizar(Long id, AsignacionConsumible datos);

	void eliminar(Long id);
}