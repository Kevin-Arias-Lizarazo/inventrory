package com.art.inventario.puerto.salida;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.art.inventario.dominio.Sesion;

public interface SesionPersistencia {

	Sesion crear(Long usuarioId, String username, String nivelAcceso, String permisos, String accessHash, String refreshHash,
			Instant fechaCreacion, Instant fechaFin, Instant accessVence);

	Optional<Sesion> porAccessHash(String accessHash);

	Optional<Sesion> porRefreshHash(String refreshHash);

	void renovarAccess(Long sesionId, String nuevoAccessHash, Instant nuevoAccessVence);

	void bloquear(Long sesionId);

	void terminar(Long sesionId);

	void bloquearPorUsuario(Long usuarioId);

	List<Sesion> vencidas(Instant ahora);

	boolean existeActivaPorUsuario(Long usuarioId);
}