package com.art.inventario.excepcion;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejoGlobalExcepciones {

	@ExceptionHandler(NoEncontradoExcepcion.class)
	public ResponseEntity<?> noEncontrado(NoEncontradoExcepcion e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
	}

	@ExceptionHandler(ConflictoExcepcion.class)
	public ResponseEntity<?> conflicto(ConflictoExcepcion e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
	}

	@ExceptionHandler(DatosInvalidosExcepcion.class)
	public ResponseEntity<?> datosInvalidos(DatosInvalidosExcepcion e) {
		return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
	}
}