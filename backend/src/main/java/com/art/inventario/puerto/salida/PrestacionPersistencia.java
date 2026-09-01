package com.art.inventario.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Prestacion;

public interface PrestacionPersistencia {

	List<Prestacion> listar();

	PaginaResultado<Prestacion> listarPagina(ConsultaPaginada consulta);

	Optional<Prestacion> porNombre(String nombre);

	Prestacion obtener(Long id);

	Prestacion guardar(Prestacion prestacion);

	void eliminar(Long id);

	boolean existeNombre(String nombre, Long excluirId);

	List<Prestacion> listarPorTipoContrato(Long tipoContratoId);
}
