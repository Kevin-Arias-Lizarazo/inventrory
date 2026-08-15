package com.art.inventario.persistencia.consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import com.art.inventario.persistencia.entidad.EntidadOrdenCompra;
public interface OrdenCompraConsultaJpa extends JpaRepository<EntidadOrdenCompra, Long> {}
