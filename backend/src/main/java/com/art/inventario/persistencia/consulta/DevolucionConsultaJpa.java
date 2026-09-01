package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.art.inventario.persistencia.entidad.EntidadDevolucion;

public interface DevolucionConsultaJpa
		extends JpaRepository<EntidadDevolucion, Long>, JpaSpecificationExecutor<EntidadDevolucion> {

	List<EntidadDevolucion> findByCompraId(Long compraId);

	boolean existsByCompraId(Long compraId);
}
