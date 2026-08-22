package com.art.inventario.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.art.inventario.dominio.NivelAcceso;

public interface NivelAccesoPersistencia {

	Optional<NivelAcceso> porCodigo(String codigo);

	Optional<Long> usuarioRaizId();

	void guardar(NivelAcceso nivel);

	List<NivelAcceso> todos();
}