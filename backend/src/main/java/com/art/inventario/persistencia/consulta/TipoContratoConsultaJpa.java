package com.art.inventario.persistencia.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadTipoContrato;

public interface TipoContratoConsultaJpa extends JpaRepository<EntidadTipoContrato, Long> {

	Optional<EntidadTipoContrato> findByNombre(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long excluirId);

	boolean existsByNombreIgnoreCase(String nombre);
}
