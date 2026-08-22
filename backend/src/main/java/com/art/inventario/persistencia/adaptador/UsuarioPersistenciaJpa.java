package com.art.inventario.persistencia.adaptador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Usuario;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.UsuarioConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadUsuario;
import com.art.inventario.puerto.salida.UsuarioPersistencia;

@Repository
@Transactional(readOnly = true)
public class UsuarioPersistenciaJpa implements UsuarioPersistencia {

	private final UsuarioConsultaJpa consulta;

	public UsuarioPersistenciaJpa(UsuarioConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Usuario> todos() {
		return consulta.findAll().stream().map(Mapeador::aDominio).toList();
	}

	@Override
	public Optional<Usuario> porUsername(String username) {
		return consulta.findByUsername(username).map(Mapeador::aDominio);
	}

	@Override
	public Optional<Usuario> porId(Long id) {
		return consulta.findById(id).map(Mapeador::aDominio);
	}

	@Override
	@Transactional
	public Usuario guardar(Usuario usuario) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(usuario)));
	}

	@Override
	public long contar() {
		return consulta.count();
	}
}