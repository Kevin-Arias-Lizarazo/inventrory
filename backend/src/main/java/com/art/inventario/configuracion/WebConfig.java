package com.art.inventario.configuracion;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.uploads.dir:uploads}")
	private String uploadsDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploads = Paths.get(uploadsDir).toAbsolutePath().normalize();
		registry.addResourceHandler("/archivos/**")
			.addResourceLocations(uploads.toUri().toString());
	}
}