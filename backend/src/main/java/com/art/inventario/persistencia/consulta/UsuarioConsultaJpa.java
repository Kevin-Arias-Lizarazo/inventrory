package com.art.inventario.persistencia.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadUsuario;

public interface UsuarioConsultaJpa extends JpaRepository<EntidadUsuario, Long> {

	Optional<EntidadUsuario> findByUsername(String username);

	boolean existsByNivelAcceso(String nivelAcceso);
}