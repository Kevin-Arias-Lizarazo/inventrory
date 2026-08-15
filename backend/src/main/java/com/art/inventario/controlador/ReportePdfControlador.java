package com.art.inventario.controlador;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.art.inventario.puerto.entrada.ReportePdfCasoDeUso;

@RestController
@RequestMapping("/api/reportes")
public class ReportePdfControlador {
	private final ReportePdfCasoDeUso servicio;
	public ReportePdfControlador(ReportePdfCasoDeUso servicio) { this.servicio = servicio; }

	@GetMapping(value = "/inventario.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> inventario() {
		return pdf("inventario.pdf", servicio.inventario());
	}

	@GetMapping(value = "/facturas.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> facturas(
			@RequestParam(required = false) String desde,
			@RequestParam(required = false) String hasta) {
		return pdf("facturas.pdf", servicio.facturas(desde, hasta));
	}

	@GetMapping(value = "/valor-inventario.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> valor() {
		return pdf("valor-inventario.pdf", servicio.valorInventario());
	}

	@GetMapping(value = "/alertas-reposicion.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> alertas() {
		return pdf("alertas-reposicion.pdf", servicio.alertasReposicion());
	}

	private ResponseEntity<byte[]> pdf(String nombre, byte[] data) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
				.contentType(MediaType.APPLICATION_PDF)
				.body(data);
	}
}
