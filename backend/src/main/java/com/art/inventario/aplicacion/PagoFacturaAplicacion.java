package com.art.inventario.aplicacion;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.PagoFactura;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.PagoFacturaCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.FacturaPersistencia;
import com.art.inventario.puerto.salida.PagoFacturaPersistencia;

@Service
public class PagoFacturaAplicacion implements PagoFacturaCasoDeUso {
	private final PagoFacturaPersistencia persistencia;
	private final FacturaPersistencia facturas;
	private final CambiosNotificador notificador;

	public PagoFacturaAplicacion(PagoFacturaPersistencia persistencia, FacturaPersistencia facturas,
			CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.facturas = facturas;
		this.notificador = notificador;
	}

	@Override
	public List<PagoFactura> listarPorFactura(Long facturaId) {
		facturas.obtener(facturaId);
		return persistencia.listarPorFactura(facturaId);
	}

	@Override
	@Transactional
	public PagoFactura crear(Long facturaId, PagoFactura pago) {
		Factura factura = facturas.obtener(facturaId);
		if (pago.getFecha() == null || pago.getFecha().isBlank()) {
			throw new DatosInvalidosExcepcion("La fecha es obligatoria");
		}
		if (pago.getMonto() == null || pago.getMonto() <= 0) {
			throw new DatosInvalidosExcepcion("El monto debe ser mayor a cero");
		}
		double total = factura.getTotal() == null ? 0d : factura.getTotal();
		double pagado = persistencia.sumaPorFactura(facturaId);
		if (pagado + pago.getMonto() > total + 0.001) {
			throw new DatosInvalidosExcepcion("El pago excede el saldo pendiente");
		}
		pago.setFacturaId(facturaId);
		PagoFactura creado = persistencia.guardar(pago);
		notificador.publicar(CambiosNotificador.RECURSO_PAGOS_FACTURA);
		notificador.publicar(CambiosNotificador.RECURSO_FACTURAS);
		return creado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_PAGOS_FACTURA);
		notificador.publicar(CambiosNotificador.RECURSO_FACTURAS);
	}
}
