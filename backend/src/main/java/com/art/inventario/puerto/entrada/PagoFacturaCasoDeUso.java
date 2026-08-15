package com.art.inventario.puerto.entrada;
import java.util.List;
import com.art.inventario.dominio.PagoFactura;
public interface PagoFacturaCasoDeUso {
	List<PagoFactura> listarPorFactura(Long facturaId);
	PagoFactura crear(Long facturaId, PagoFactura pago);
	void eliminar(Long id);
}
