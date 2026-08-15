package com.art.inventario.puerto.salida;

import java.util.List;

import com.art.inventario.dominio.Contrato;

public interface ContratoPersistencia {

	List<Contrato> listar();

	Contrato obtener(Long id);

	Contrato guardar(Contrato contrato);

	void eliminar(Long id);

	boolean empleadoContratado(Long empleadoId);

	List<Long> empleadosContratados();

	boolean tieneContratos(Long empleadoId);
}