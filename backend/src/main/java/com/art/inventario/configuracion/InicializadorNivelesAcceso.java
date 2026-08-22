package com.art.inventario.configuracion;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.art.inventario.dominio.NivelAcceso;
import com.art.inventario.puerto.salida.NivelAccesoPersistencia;

@Component
public class InicializadorNivelesAcceso implements ApplicationRunner {

	private final NivelAccesoPersistencia niveles;

	public InicializadorNivelesAcceso(NivelAccesoPersistencia niveles) {
		this.niveles = niveles;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!niveles.todos().isEmpty()) {
			return;
		}
		for (NivelAcceso n : List.of(
				new NivelAcceso("ROOT", "Raíz"),
				new NivelAcceso("ADMIN", "Administrador"),
				new NivelAcceso("USUARIO", "Usuario"),
				new NivelAcceso("LECTOR", "Lector"))) {
			niveles.guardar(n);
		}
	}
}