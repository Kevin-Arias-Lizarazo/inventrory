package com.art.inventario.persistencia.consulta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.art.inventario.persistencia.entidad.EntidadPagoFactura;

public interface PagoFacturaConsultaJpa extends JpaRepository<EntidadPagoFactura, Long> {
	List<EntidadPagoFactura> findByFacturaIdOrderByIdAsc(Long facturaId);
	boolean existsByFacturaId(Long facturaId);
	@Query("select coalesce(sum(p.monto),0) from EntidadPagoFactura p where p.facturaId = :facturaId")
	Double sumaPorFactura(@Param("facturaId") Long facturaId);
	void deleteByFacturaId(Long facturaId);
}
