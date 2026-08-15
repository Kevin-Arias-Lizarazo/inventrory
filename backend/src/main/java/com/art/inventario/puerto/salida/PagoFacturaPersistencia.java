package com.art.inventario.puerto.salida;
import java.util.List;
import com.art.inventario.dominio.PagoFactura;
public interface PagoFacturaPersistencia {
	List<PagoFactura> listarPorFactura(Long facturaId);
	PagoFactura obtener(Long id);
	PagoFactura guardar(PagoFactura pago);
	void eliminar(Long id);
	void eliminarPorFactura(Long facturaId);
	double sumaPorFactura(Long facturaId);
	boolean tienePorFactura(Long facturaId);
}
