package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.TipoContratoPrestacion;

public interface TipoContratoPrestacionPersistencia {

	List<TipoContratoPrestacion> listar();

	List<TipoContratoPrestacion> listarPorTipoContrato(Long tipoContratoId);

	TipoContratoPrestacion guardar(TipoContratoPrestacion relacion);

	void eliminarPorTipoContrato(Long tipoContratoId);
}
