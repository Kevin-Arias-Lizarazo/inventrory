package com.art.inventario.persistencia.consulta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadContratoPrestacionCalculada;

public interface ContratoPrestacionCalculadaConsultaJpa
		extends JpaRepository<EntidadContratoPrestacionCalculada, Long> {

	List<EntidadContratoPrestacionCalculada> findByContratoId(Long contratoId);

	void deleteByContratoId(Long contratoId);
}
