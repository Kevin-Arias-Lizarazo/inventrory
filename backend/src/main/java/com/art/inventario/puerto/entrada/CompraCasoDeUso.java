package com.art.inventario.puerto.entrada;

import java.util.List;

import com.art.inventario.dominio.Compra;

public interface CompraCasoDeUso {

	List<Compra> listar();

	Compra obtener(Long id);

	Compra crear(Compra compra);

	Compra actualizar(Long id, Compra datos);

	void eliminar(Long id);
}