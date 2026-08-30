package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.ContratoPrestacionExtra;

/**
 * Output port for the manually-managed extra payments of a contract.
 */
public interface ContratoPrestacionExtraPersistencia {

	List<ContratoPrestacionExtra> listarPorContrato(Long contratoId);

	ContratoPrestacionExtra obtener(Long id);

	ContratoPrestacionExtra guardar(ContratoPrestacionExtra extra);

	void eliminar(Long id);

	boolean existe(Long id);
}
