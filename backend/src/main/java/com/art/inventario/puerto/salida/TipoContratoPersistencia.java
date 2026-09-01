package com.art.inventario.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.TipoContrato;

public interface TipoContratoPersistencia {

	List<TipoContrato> listar();

	PaginaResultado<TipoContrato> listarPagina(ConsultaPaginada consulta);

	Optional<TipoContrato> porNombre(String nombre);

	TipoContrato obtener(Long id);

	TipoContrato guardar(TipoContrato tipoContrato);

	void eliminar(Long id);

	boolean existeNombre(String nombre, Long excluirId);
}
