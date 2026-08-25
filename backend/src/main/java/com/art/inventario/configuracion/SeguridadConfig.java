package com.art.inventario.configuracion;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

	private static final String[] PUBLICAS = {
			"/",
			"/index.html",
			"/assets/**",
			"/favicon.ico",
			"/archivos/**",
			"/api/auth/login",
			"/api/auth/renovar",
			"/api/auth/csrf",
			"/api/auth/cambiar-contrasena-usuario",
			"/api/instalacion/**",
			"/api/cambios/**",
			"/error"
	};

	@Bean
	UserDetailsService userDetailsService() {
		return username -> {
			throw new UsernameNotFoundException(username);
		};
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	FilterRegistrationBean<FiltroRegistroPeticiones> registroNoGlobal(FiltroRegistroPeticiones filtro) {
		FilterRegistrationBean<FiltroRegistroPeticiones> rb = new FilterRegistrationBean<>(filtro);
		rb.setEnabled(false);
		return rb;
	}

	@Bean
	FilterRegistrationBean<TokenAuthFiltro> tokenNoGlobal(TokenAuthFiltro filtro) {
		FilterRegistrationBean<TokenAuthFiltro> rb = new FilterRegistrationBean<>(filtro);
		rb.setEnabled(false);
		return rb;
	}

	@Bean
	FilterRegistrationBean<FiltroLectorSoloLectura> lectorNoGlobal(FiltroLectorSoloLectura filtro) {
		FilterRegistrationBean<FiltroLectorSoloLectura> rb = new FilterRegistrationBean<>(filtro);
		rb.setEnabled(false);
		return rb;
	}

	@Bean
	FilterRegistrationBean<SpaForwardFiltro> spaNoGlobal(SpaForwardFiltro filtro) {
		FilterRegistrationBean<SpaForwardFiltro> rb = new FilterRegistrationBean<>(filtro);
		rb.setEnabled(false);
		return rb;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, TokenAuthFiltro tokenFiltro,
			FiltroLectorSoloLectura soloLectura, FiltroRegistroPeticiones registro, SpaForwardFiltro spa) throws Exception {
		http
			.addFilterBefore(spa, org.springframework.security.web.csrf.CsrfFilter.class)
			.csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers(
						"/api/auth/login",
						"/api/auth/cambiar-contrasena-usuario",
						"/api/auth/csrf",
						"/api/instalacion/**"))
			.addFilterBefore(tokenFiltro, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(soloLectura, TokenAuthFiltro.class)
			.addFilterAfter(registro, FiltroLectorSoloLectura.class)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(PUBLICAS).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/auth/me", "/api/auth/logout").authenticated()
				.requestMatchers("/api/usuarios/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/backup/exportar-completo").hasAnyRole("ADMIN", "ROOT")
				.requestMatchers("/api/backup/**").hasRole("ADMIN")
				.requestMatchers("/api/importar/**").hasRole("ADMIN")
				.requestMatchers("/api/auditoria/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authEx) -> {
					response.setStatus(401);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.getWriter().write("{\"mensaje\":\"No autorizado\"}");
				})
				.accessDeniedHandler((request, response, denied) -> {
					response.setStatus(403);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.getWriter().write("{\"mensaje\":\"Acceso denegado\"}");
				}));
		return http.build();
	}
}