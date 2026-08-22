package com.art.inventario.configuracion;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.art.inventario.dominio.Accion;
import com.art.inventario.dominio.EventoLog;
import com.art.inventario.puerto.salida.RegistroAuditoria;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FiltroRegistroPeticiones extends OncePerRequestFilter {

	private final RegistroAuditoria auditoria;

	public FiltroRegistroPeticiones(RegistroAuditoria auditoria) {
		this.auditoria = auditoria;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api/")) {
			chain.doFilter(request, response);
			return;
		}
		long inicio = System.nanoTime();
		try {
			chain.doFilter(request, response);
		} finally {
			EventoLog e = new EventoLog();
			e.setFecha(Instant.now().toString());
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado ua) {
				e.setUsuario(ua.getUsername());
				e.setRol(ua.getNivel());
			} else {
				e.setUsuario("anonimo");
			}
			e.setIp(clienteIp(request));
			e.setMetodo(request.getMethod());
			e.setRuta(rutaCompleta(request));
			e.setAccion(Accion.PETICION);
			e.setResultado(String.valueOf(response.getStatus()));
			e.setDuracionMs((System.nanoTime() - inicio) / 1_000_000);
			auditoria.registrar(e);
		}
	}

	private String rutaCompleta(HttpServletRequest request) {
		String query = request.getQueryString();
		return query == null || query.isBlank()
				? request.getRequestURI()
				: request.getRequestURI() + "?" + query;
	}

	private String clienteIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isBlank()) {
			ip = request.getRemoteAddr();
		}
		return ip == null ? "desconocida" : ip;
	}
}