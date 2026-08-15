package com.art.inventario.controlador;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.art.inventario.dominio.PagoFactura;
import com.art.inventario.puerto.entrada.PagoFacturaCasoDeUso;

@RestController
@RequestMapping("/api")
public class PagoFacturaControlador {
	private final PagoFacturaCasoDeUso servicio;
	public PagoFacturaControlador(PagoFacturaCasoDeUso servicio) { this.servicio = servicio; }

	@GetMapping("/facturas/{facturaId}/pagos")
	public ResponseEntity<List<PagoFactura>> listar(@PathVariable Long facturaId) {
		return ResponseEntity.ok(servicio.listarPorFactura(facturaId));
	}

	@PostMapping("/facturas/{facturaId}/pagos")
	public ResponseEntity<PagoFactura> crear(@PathVariable Long facturaId, @RequestBody PagoFactura pago) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(facturaId, pago));
	}

	@DeleteMapping("/pagos-factura/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
