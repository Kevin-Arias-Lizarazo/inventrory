package com.art.inventario.configuracion;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ControlLogin {

	private static final int MAX_FALLOS = 10;
	private static final Duration VENTANA = Duration.ofMinutes(5);

	private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();
	private final Map<String, Instant> ultimoCambioClave = new ConcurrentHashMap<>();

	public boolean permitido(String username) {
		prune(username);
		Deque<Instant> cola = fallos.getOrDefault(username, new ArrayDeque<>());
		return cola.size() < MAX_FALLOS;
	}

	public int fallosRecientes(String username) {
		prune(username);
		return fallos.getOrDefault(username, new ArrayDeque<>()).size();
	}

	public void registrarFallo(String username) {
		fallos.computeIfAbsent(username, k -> new ArrayDeque<>()).addLast(Instant.now());
		prune(username);
	}

	public void limpiar(String username) {
		fallos.remove(username);
	}

	public void registrarCambioClave(String username) {
		ultimoCambioClave.put(username, Instant.now());
	}

	public Instant ultimoCambioClave(String username) {
		return ultimoCambioClave.get(username);
	}

	private void prune(String username) {
		Deque<Instant> cola = fallos.get(username);
		if (cola == null) {
			return;
		}
		Instant limite = Instant.now().minus(VENTANA);
		while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
			cola.removeFirst();
		}
		if (cola.isEmpty()) {
			fallos.remove(username);
		}
	}
}