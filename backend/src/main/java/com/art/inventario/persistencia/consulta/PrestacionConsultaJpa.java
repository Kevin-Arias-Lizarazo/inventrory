package com.art.inventario.persistencia.consulta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadPrestacion;

public interface PrestacionConsultaJpa extends JpaRepository<EntidadPrestacion, Long>,
		JpaSpecificationExecutor<EntidadPrestacion> {

	Optional<EntidadPrestacion> findByNombre(String nombre);

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long excluirId);

	@Query("select p from EntidadPrestacion p join EntidadTipoContratoPrestacion m on m.prestacion.id = p.id "
			+ "where m.tipoContrato.id = :tipoContratoId")
	List<EntidadPrestacion> findByTipoContratoId(@Param("tipoContratoId") Long tipoContratoId);
}
