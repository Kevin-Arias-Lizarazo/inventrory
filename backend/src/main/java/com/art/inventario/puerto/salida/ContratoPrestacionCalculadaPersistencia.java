package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.ContratoPrestacionCalculada;

/**
 * Output port for the calculated-benefits snapshot of a contract. Rows are
 * literal, immutable copies; replacement is always delete-then-insert.
 */
public interface ContratoPrestacionCalculadaPersistencia {

	List<ContratoPrestacionCalculada> listarPorContrato(Long contratoId);

	void guardar(ContratoPrestacionCalculada linea);

	void eliminarPorContrato(Long contratoId);
}
