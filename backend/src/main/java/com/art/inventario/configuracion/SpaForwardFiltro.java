package com.art.inventario.configuracion;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SpaForwardFiltro extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String uri = request.getRequestURI();
		if (esRutaSpa(uri)) {
			request.getRequestDispatcher("/index.html").forward(request, response);
			return;
		}
		chain.doFilter(request, response);
	}

	private boolean esRutaSpa(String uri) {
		if (uri == null || "/".equals(uri)) {
			return false;
		}
		if (uri.startsWith("/api/") || uri.startsWith("/archivos/") || uri.startsWith("/assets/")
				|| uri.startsWith("/error")) {
			return false;
		}
		String ultimoSegmento = uri.substring(uri.lastIndexOf('/') + 1);
		return !ultimoSegmento.contains(".");
	}
}