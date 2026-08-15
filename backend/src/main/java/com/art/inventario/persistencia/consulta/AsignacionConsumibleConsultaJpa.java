package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadAsignacionConsumible;

public interface AsignacionConsumibleConsultaJpa extends JpaRepository<EntidadAsignacionConsumible, Long> {
}