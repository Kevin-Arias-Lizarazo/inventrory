package com.art.inventario.notificacion;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.art.inventario.puerto.salida.CambiosNotificador;

@Service
public class NotificadorCambios implements CambiosNotificador {

	private final List<SseEmitter> emisores = new CopyOnWriteArrayList<>();

	@Override
	public SseEmitter suscribir() {
		SseEmitter emisor = new SseEmitter(0L);
		emisores.add(emisor);
		emisor.onCompletion(() -> emisores.remove(emisor));
		emisor.onTimeout(() -> emisores.remove(emisor));
		emisor.onError(t -> emisores.remove(emisor));
		try {
			emisor.send(SseEmitter.event().data("{\"recurso\":\"conexion\"}"));
		} catch (Exception e) {
			emisores.remove(emisor);
		}
		return emisor;
	}

	@Override
	public void publicar(String recurso) {
		for (SseEmitter emisor : emisores) {
			try {
				emisor.send(SseEmitter.event().data(json(recurso)));
			} catch (Exception e) {
				emisores.remove(emisor);
			}
		}
	}

	private static String json(String recurso) {
		return "{\"recurso\":\"" + recurso + "\"}";
	}
}