package com.art.inventario.persistencia.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import com.art.inventario.persistencia.entidad.EntidadAjuste;

public interface AjusteConsultaJpa extends JpaRepository<EntidadAjuste, Long> {
}
