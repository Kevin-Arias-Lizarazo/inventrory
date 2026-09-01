package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.TipoContrato;

public interface TipoContratoCasoDeUso {

	List<TipoContrato> listar();

	PaginaResultado<TipoContrato> listarPagina(ConsultaPaginada consulta);

	TipoContrato obtener(Long id);

	TipoContrato crear(TipoContrato tipoContrato);

	TipoContrato actualizar(Long id, TipoContrato datos);

	void eliminar(Long id);
}
