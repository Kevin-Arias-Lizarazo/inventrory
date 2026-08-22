package com.art.inventario.controlador;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.art.inventario.aplicacion.dto.RespuestaLogin;
import com.art.inventario.aplicacion.dto.UsuarioRespuesta;
import com.art.inventario.puerto.entrada.AuthCasoDeUso;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

	private static final String COOKIE_REFRESH = "refresh_token";
	private static final long MAX_AGE_REFRESH = 60 * 60 * 12;

	private final AuthCasoDeUso servicio;

	public AuthControlador(AuthCasoDeUso servicio) {
		this.servicio = servicio;
	}

	@PostMapping("/login")
	public ResponseEntity<RespuestaLogin> login(@RequestBody Map<String, String> cuerpo, HttpServletRequest request,
			HttpServletResponse response) {
		String ip = request.getRemoteAddr();
		RespuestaLogin r = servicio.login(cuerpo.get("username"), cuerpo.get("password"), ip);
		guardarRefreshCookie(response, r.getRefreshToken());
		return ResponseEntity.ok(r);
	}

	@PostMapping("/renovar")
	public ResponseEntity<RespuestaLogin> renovar(HttpServletRequest request, HttpServletResponse response) {
		RespuestaLogin r = servicio.renovar(refreshDe(request));
		guardarRefreshCookie(response, r.getRefreshToken());
		return ResponseEntity.ok(r);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		servicio.logout(refreshDe(request));
		ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH, "")
				.httpOnly(true)
				.path("/api/auth")
				.maxAge(0)
				.sameSite("Strict")
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/cambiar-contrasena")
	public ResponseEntity<Void> cambiarContrasena(@RequestBody Map<String, String> cuerpo) {
		servicio.cambiarContrasena(cuerpo.get("username"), cuerpo.get("actual"), cuerpo.get("nueva"));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/cambiar-contrasena-usuario")
	public ResponseEntity<Void> cambiarContrasenaTercero(@RequestBody Map<String, String> cuerpo) {
		String usuarioId = cuerpo.get("usuarioId");
		Long objetivoId = usuarioId == null || usuarioId.isBlank() ? null : Long.parseLong(usuarioId);
		servicio.cambiarContrasenaTercero(objetivoId, cuerpo.get("contrasena"), cuerpo.get("secretoRoot"));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<UsuarioRespuesta> me() {
		return ResponseEntity.ok(servicio.usuarioActual());
	}

	@GetMapping("/csrf")
	public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
		return ResponseEntity.ok(Map.of("token", token.getToken()));
	}

	private void guardarRefreshCookie(HttpServletResponse response, String refreshToken) {
		ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH, refreshToken)
				.httpOnly(true)
				.path("/api/auth")
				.maxAge(MAX_AGE_REFRESH)
				.sameSite("Strict")
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private String refreshDe(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (COOKIE_REFRESH.equals(c.getName())) {
					return c.getValue();
				}
			}
		}
		return null;
	}
}