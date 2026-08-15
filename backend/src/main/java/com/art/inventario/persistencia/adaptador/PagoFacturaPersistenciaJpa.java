package com.art.inventario.persistencia.adaptador;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.art.inventario.dominio.PagoFactura;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.PagoFacturaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadPagoFactura;
import com.art.inventario.puerto.salida.PagoFacturaPersistencia;

@Repository
@Transactional(readOnly = true)
public class PagoFacturaPersistenciaJpa implements PagoFacturaPersistencia {
	private final PagoFacturaConsultaJpa consulta;
	public PagoFacturaPersistenciaJpa(PagoFacturaConsultaJpa consulta) { this.consulta = consulta; }
	@Override public List<PagoFactura> listarPorFactura(Long facturaId) {
		return consulta.findByFacturaIdOrderByIdAsc(facturaId).stream().map(Mapeador::aDominio).toList();
	}
	@Override public PagoFactura obtener(Long id) {
		return consulta.findById(id).map(Mapeador::aDominio)
			.orElseThrow(() -> new NoEncontradoExcepcion("Pago no encontrado"));
	}
	@Override @Transactional public PagoFactura guardar(PagoFactura pago) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(pago)));
	}
	@Override @Transactional public void eliminar(Long id) {
		if (!consulta.existsById(id)) throw new NoEncontradoExcepcion("Pago no encontrado");
		consulta.deleteById(id);
	}
	@Override @Transactional public void eliminarPorFactura(Long facturaId) { consulta.deleteByFacturaId(facturaId); }
	@Override public double sumaPorFactura(Long facturaId) {
		Double s = consulta.sumaPorFactura(facturaId);
		return s == null ? 0d : s;
	}
	@Override public boolean tienePorFactura(Long facturaId) { return consulta.existsByFacturaId(facturaId); }
}
