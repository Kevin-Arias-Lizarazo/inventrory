package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadMovimientoMaterial;

public interface MovimientoMaterialConsultaJpa
		extends JpaRepository<EntidadMovimientoMaterial, Long>, JpaSpecificationExecutor<EntidadMovimientoMaterial> {

	List<EntidadMovimientoMaterial> findByMaterialIdOrderByFechaDesc(Long materialId);

	boolean existsByMaterialId(Long materialId);
}