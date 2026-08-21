package com.art.inventario.configuracion;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FiltroLectorSoloLectura extends OncePerRequestFilter {

	private static final String[] PERMITIDAS_PARA_LECTOR = {
			"/api/auth/logout",
			"/api/auth/cambiar-contrasena",
			"/api/auth/me"
	};

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean esLector = auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LECTOR"));
		if (esLector && !esMetodoLectura(request)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json");
			response.getWriter().write("{\"mensaje\":\"El rol LECTOR solo puede consultar información\"}");
			return;
		}
		chain.doFilter(request, response);
	}

	private boolean esMetodoLectura(HttpServletRequest request) {
		String metodo = request.getMethod();
		if ("GET".equalsIgnoreCase(metodo) || "HEAD".equalsIgnoreCase(metodo) || "OPTIONS".equalsIgnoreCase(metodo)) {
			return true;
		}
		String ruta = request.getRequestURI();
		for (String p : PERMITIDAS_PARA_LECTOR) {
			if (ruta.equals(p)) {
				return true;
			}
		}
		return false;
	}
}