package com.art.inventario.puerto.salida;

import java.util.List;
import java.util.Optional;

import com.art.inventario.dominio.Usuario;

public interface UsuarioPersistencia {

	List<Usuario> todos();

	Optional<Usuario> porUsername(String username);

	Optional<Usuario> porId(Long id);

	Usuario guardar(Usuario usuario);

	long contar();
}