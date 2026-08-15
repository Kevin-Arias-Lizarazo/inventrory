package com.art.inventario.controlador;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.art.inventario.puerto.salida.CambiosNotificador;

@RestController
public class CambiosControlador {

	private final CambiosNotificador notificador;

	public CambiosControlador(CambiosNotificador notificador) {
		this.notificador = notificador;
	}

	@GetMapping(path = "/api/cambios/suscripcion", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter suscribir() {
		return notificador.suscribir();
	}
}