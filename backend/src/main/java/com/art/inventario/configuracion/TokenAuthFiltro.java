package com.art.inventario.configuracion;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.art.inventario.dominio.Sesion;
import com.art.inventario.puerto.salida.SesionPersistencia;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthFiltro extends OncePerRequestFilter {

	private final SesionPersistencia sesiones;

	public TokenAuthFiltro(SesionPersistencia sesiones) {
		this.sesiones = sesiones;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String autorizacion = request.getHeader("Authorization");
		if (autorizacion != null && autorizacion.startsWith("Bearer ")) {
			String token = autorizacion.substring(7);
			sesiones.porAccessHash(SeguridadUtil.hash(token)).ifPresent(sesion -> {
				if (valida(sesion)) {
					UsuarioAutenticado ua = new UsuarioAutenticado(
							sesion.getUsuarioId(), sesion.getUsername(), sesion.getUsername(), sesion.getNivelAcceso());
					var auth = new UsernamePasswordAuthenticationToken(ua, null,
							List.of(new SimpleGrantedAuthority("ROLE_" + sesion.getNivelAcceso())));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			});
		}
		chain.doFilter(request, response);
	}

	private boolean valida(Sesion sesion) {
		if (sesion.bloqueada()) {
			return false;
		}
		Instant ahora = Instant.now();
		try {
			boolean vence = Instant.parse(sesion.getFechaFin()).isAfter(ahora);
			boolean accesoVigente = Instant.parse(sesion.getAccessVence()).isAfter(ahora);
			return vence && accesoVigente;
		} catch (Exception e) {
			return false;
		}
	}
}