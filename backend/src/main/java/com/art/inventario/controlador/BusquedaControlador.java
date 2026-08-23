package com.art.inventario.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.ResultadoBusqueda;
import com.art.inventario.puerto.entrada.BusquedaCasoDeUso;

@RestController
@RequestMapping("/api/buscar")
public class BusquedaControlador {

	private final BusquedaCasoDeUso servicio;

	public BusquedaControlador(BusquedaCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@GetMapping
	public ResponseEntity<List<ResultadoBusqueda>> buscar(@RequestParam(required = false) String q) {
		return ResponseEntity.ok(servicio.buscar(q));
	}
}