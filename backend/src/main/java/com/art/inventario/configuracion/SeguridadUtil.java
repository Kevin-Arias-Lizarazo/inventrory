package com.art.inventario.configuracion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class SeguridadUtil {

	private static final SecureRandom ALEATORIO = new SecureRandom();
	private static final char[] ALFABETO = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

	private SeguridadUtil() {
	}

	public static String generarToken(int longitud) {
		StringBuilder sb = new StringBuilder(longitud);
		for (int i = 0; i < longitud; i++) {
			sb.append(ALFABETO[ALEATORIO.nextInt(ALFABETO.length)]);
		}
		return sb.toString();
	}

	public static String hash(String valor) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(valor.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(64);
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo calcular el hash", e);
		}
	}
}