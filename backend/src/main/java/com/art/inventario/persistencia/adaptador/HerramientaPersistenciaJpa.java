package com.art.inventario.persistencia.adaptador;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.HerramientaConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.puerto.salida.HerramientaPersistencia;

@Repository
@Transactional(readOnly = true)
public class HerramientaPersistenciaJpa implements HerramientaPersistencia {

	private final HerramientaConsultaJpa consulta;

	public HerramientaPersistenciaJpa(HerramientaConsultaJpa consulta) {
		this.consulta = consulta;
	}

	@Override
	public List<Herramienta> listar() {
		return Mapeador.aDominioHerramientas(consulta.findAll());
	}

	@Override
	public PaginaResultado<Herramienta> listarPagina(int pagina, int tamano) {
		Page<EntidadHerramienta> page = consulta.findAll(PageRequest.of(pagina, tamano));
		List<Herramienta> contenido = page.getContent().stream().map(Mapeador::aDominio).toList();
		return new PaginaResultado<>(contenido, pagina, tamano, page.getTotalElements(), page.getTotalPages());
	}

	@Override
	public boolean existeNombre(String nombre, Long excluirId) {
		return consulta.contarPorNombre(nombre, excluirId == null ? -1L : excluirId) > 0;
	}

	@Override
	public Herramienta obtener(Long id) {
		return consulta.findById(id)
				.map(Mapeador::aDominio)
				.orElseThrow(() -> new NoEncontradoExcepcion("Herramienta no encontrada"));
	}

	@Override
	public Herramienta obtenerPorCodigo(String codigo) {
		EntidadHerramienta entidad = consulta.findByCodigo(codigo);
		if (entidad == null) {
			throw new NoEncontradoExcepcion("Herramienta no encontrada");
		}
		return Mapeador.aDominio(entidad);
	}

	@Override
	@Transactional
	public Herramienta guardar(Herramienta herramienta) {
		return Mapeador.aDominio(consulta.save(Mapeador.aEntidad(herramienta)));
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Herramienta no encontrada");
		}
		consulta.deleteById(id);
	}
}