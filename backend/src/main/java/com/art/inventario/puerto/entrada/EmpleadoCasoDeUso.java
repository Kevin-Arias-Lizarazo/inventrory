package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Empleado;

public interface EmpleadoCasoDeUso {

	List<Empleado> listar(String q, boolean soloContratados);

	PaginaResultado<Empleado> listarPagina(ConsultaPaginada consulta);


	Empleado obtener(Long id);

	Empleado crear(Empleado empleado);

	Empleado actualizar(Long id, Empleado datos);

	void eliminar(Long id);
}