package com.art.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.puerto.entrada.PagoFacturaCasoDeUso;

@RestController
@RequestMapping("/api/pagos-factura")
public class PagoFacturaControlador {

	private final PagoFacturaCasoDeUso servicio;

	public PagoFacturaControlador(PagoFacturaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
