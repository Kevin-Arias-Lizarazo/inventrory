package com.art.inventario.persistencia.adaptador;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.art.inventario.dominio.Rol;
import com.art.inventario.dominio.Sesion;
import com.art.inventario.puerto.salida.SesionPersistencia;

@Repository
public class SesionPersistenciaSqlite implements SesionPersistencia {

	private static final String TABLA = "CREATE TABLE IF NOT EXISTS sesiones ("
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "access_hash TEXT UNIQUE NOT NULL,"
			+ "refresh_hash TEXT UNIQUE NOT NULL,"
			+ "access_vence TEXT NOT NULL,"
			+ "usuario_id INTEGER NOT NULL,"
			+ "username TEXT NOT NULL,"
			+ "rol TEXT NOT NULL,"
			+ "permisos TEXT,"
			+ "fecha_creacion TEXT NOT NULL,"
			+ "fecha_fin TEXT NOT NULL,"
			+ "bloqueada INTEGER NOT NULL DEFAULT 0)";

	private final JdbcTemplate jdbc;
	private final RowMapper<Sesion> fila = this::mapear;

	public SesionPersistenciaSqlite(JdbcTemplate sesionJdbcTemplate) {
		this.jdbc = sesionJdbcTemplate;
		jdbc.execute(TABLA);
	}

	@Override
	public Sesion crear(Long usuarioId, String username, Rol rol, String permisos, String accessHash, String refreshHash,
			Instant fechaCreacion, Instant fechaFin, Instant accessVence) {
		KeyHolder kh = new GeneratedKeyHolder();
		jdbc.update(con -> {
			var ps = con.prepareStatement(
					"INSERT INTO sesiones (access_hash, refresh_hash, access_vence, usuario_id, username, rol, permisos, fecha_creacion, fecha_fin, bloqueada) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)");
			ps.setString(1, accessHash);
			ps.setString(2, refreshHash);
			ps.setString(3, accessVence.toString());
			ps.setLong(4, usuarioId);
			ps.setString(5, username);
			ps.setString(6, rol.name());
			ps.setString(7, permisos);
			ps.setString(8, fechaCreacion.toString());
			ps.setString(9, fechaFin.toString());
			return ps;
		}, kh);
		Sesion s = new Sesion();
		s.setId(kh.getKey() == null ? null : kh.getKey().longValue());
		s.setAccessHash(accessHash);
		s.setRefreshHash(refreshHash);
		s.setAccessVence(accessVence.toString());
		s.setUsuarioId(usuarioId);
		s.setUsername(username);
		s.setRol(rol);
		s.setPermisos(permisos);
		s.setFechaCreacion(fechaCreacion.toString());
		s.setFechaFin(fechaFin.toString());
		s.setBloqueada(false);
		return s;
	}

	@Override
	public Optional<Sesion> porAccessHash(String accessHash) {
		List<Sesion> r = jdbc.query("SELECT * FROM sesiones WHERE access_hash = ?", fila, accessHash);
		return r.stream().findFirst();
	}

	@Override
	public Optional<Sesion> porRefreshHash(String refreshHash) {
		List<Sesion> r = jdbc.query("SELECT * FROM sesiones WHERE refresh_hash = ?", fila, refreshHash);
		return r.stream().findFirst();
	}

	@Override
	public void renovarAccess(Long sesionId, String nuevoAccessHash, Instant nuevoAccessVence) {
		jdbc.update("UPDATE sesiones SET access_hash = ?, access_vence = ? WHERE id = ?",
				nuevoAccessHash, nuevoAccessVence.toString(), sesionId);
	}

	@Override
	public void bloquear(Long sesionId) {
		jdbc.update("UPDATE sesiones SET bloqueada = 1 WHERE id = ?", sesionId);
	}

	@Override
	public void terminar(Long sesionId) {
		jdbc.update("DELETE FROM sesiones WHERE id = ?", sesionId);
	}

	@Override
	public void bloquearPorUsuario(Long usuarioId) {
		jdbc.update("UPDATE sesiones SET bloqueada = 1 WHERE usuario_id = ?", usuarioId);
	}

	@Override
	public List<Sesion> vencidas(Instant ahora) {
		return jdbc.query("SELECT * FROM sesiones", fila).stream()
				.filter(s -> Instant.parse(s.getFechaFin()).isBefore(ahora) || s.bloqueada())
				.toList();
	}

	@Override
	public boolean existeActivaPorUsuario(Long usuarioId) {
		Long n = jdbc.queryForObject(
				"SELECT COUNT(*) FROM sesiones WHERE usuario_id = ? AND bloqueada = 0", Long.class, usuarioId);
		return n != null && n > 0;
	}

	private Sesion mapear(ResultSet rs, int i) throws SQLException {
		Sesion s = new Sesion();
		s.setId(rs.getLong("id"));
		s.setAccessHash(rs.getString("access_hash"));
		s.setRefreshHash(rs.getString("refresh_hash"));
		s.setAccessVence(rs.getString("access_vence"));
		s.setUsuarioId(rs.getLong("usuario_id"));
		s.setUsername(rs.getString("username"));
		s.setRol(Rol.valueOf(rs.getString("rol")));
		s.setPermisos(rs.getString("permisos"));
		s.setFechaCreacion(rs.getString("fecha_creacion"));
		s.setFechaFin(rs.getString("fecha_fin"));
		s.setBloqueada(rs.getInt("bloqueada") == 1);
		return s;
	}

	public static void limpiarDirectorio(String dir) {
		try {
			Path p = Paths.get(dir).toAbsolutePath().normalize();
			Files.createDirectories(p);
		} catch (Exception e) {
			// ignorar
		}
	}
}