package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadContratoPrestacionExtra;

public interface ContratoPrestacionExtraConsultaJpa
		extends JpaRepository<EntidadContratoPrestacionExtra, Long> {

	List<EntidadContratoPrestacionExtra> findByContratoId(Long contratoId);
}
