package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.PagoFactura;
import com.art.inventario.puerto.entrada.FacturaCasoDeUso;
import com.art.inventario.puerto.entrada.PagoFacturaCasoDeUso;

@RestController
@RequestMapping("/api/facturas")
public class FacturaControlador {

	private final FacturaCasoDeUso servicio;
	private final PagoFacturaCasoDeUso pagoServicio;

	public FacturaControlador(FacturaCasoDeUso servicio, PagoFacturaCasoDeUso pagoServicio) {
		this.servicio = servicio;
		this.pagoServicio = pagoServicio;
	}

	@GetMapping
	public ResponseEntity<List<Factura>> listar() {
		return ResponseEntity.ok(servicio.listar());
	}

	@GetMapping("/paginado")
	public ResponseEntity<PaginaResultado<Factura>> listarPagina(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Long proveedorId,
			@RequestParam(required = false) String fecha,
			@RequestParam(required = false) String estadoPago,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "30") int tamano) {
		return ResponseEntity.ok(servicio.listarPagina(q, proveedorId, fecha, estadoPago, pagina, tamano));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Factura> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(servicio.obtener(id));
	}

	@PostMapping
	public ResponseEntity<Factura> crear(@RequestBody Factura factura) {
		return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(factura));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Factura> actualizar(@PathVariable Long id, @RequestBody Factura datos) {
		return ResponseEntity.ok(servicio.actualizar(id, datos));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		servicio.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{facturaId}/pagos")
	public ResponseEntity<List<PagoFactura>> listarPagos(@PathVariable Long facturaId) {
		return ResponseEntity.ok(pagoServicio.listarPorFactura(facturaId));
	}

	@PostMapping("/{facturaId}/pagos")
	public ResponseEntity<PagoFactura> crearPago(@PathVariable Long facturaId, @RequestBody PagoFactura pago) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pagoServicio.crear(facturaId, pago));
	}
}