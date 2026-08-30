package com.art.inventario.persistencia.consulta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.art.inventario.persistencia.entidad.EntidadTipoContratoPrestacion;

public interface TipoContratoPrestacionConsultaJpa extends JpaRepository<EntidadTipoContratoPrestacion, Long> {

	List<EntidadTipoContratoPrestacion> findByTipoContratoId(Long tipoContratoId);

	Optional<EntidadTipoContratoPrestacion> findByTipoContratoIdAndPrestacionId(Long tipoContratoId, Long prestacionId);

	@Modifying
	@Query("delete from EntidadTipoContratoPrestacion m where m.tipoContrato.id = :tipoContratoId")
	void eliminarPorTipoContrato(@Param("tipoContratoId") Long tipoContratoId);
}
