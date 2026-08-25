package com.art.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventarioApplication {

	public static void main(String[] args) {
		// La restauración pendiente debe aplicarse ANTES de abrir la DataSource:
		// con el pool abierto, SQLite mantiene el inode/lock del archivo activo y
		// el swap fallaría (Windows) o se perdería (Linux seguiría el inode viejo).
		RestauracionPendiente.aplicarSiExiste();
		SpringApplication.run(InventarioApplication.class, args);
	}

}
