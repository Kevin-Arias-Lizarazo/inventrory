package com.art.inventario.persistencia.adaptador;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Ajuste;
import com.art.inventario.dominio.LineaAjuste;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.Mapeador;
import com.art.inventario.persistencia.consulta.AjusteConsultaJpa;
import com.art.inventario.persistencia.consulta.LineaAjusteConsultaJpa;
import com.art.inventario.persistencia.entidad.EntidadAjuste;
import com.art.inventario.persistencia.entidad.EntidadLineaAjuste;
import com.art.inventario.puerto.salida.AjusteConsultaSalida;
import com.art.inventario.puerto.salida.AjustePersistencia;

@Repository
@Transactional(readOnly = true)
public class AjustePersistenciaJpa implements AjustePersistencia, AjusteConsultaSalida {

	private final AjusteConsultaJpa consulta;
	private final LineaAjusteConsultaJpa lineasConsulta;

	public AjustePersistenciaJpa(AjusteConsultaJpa consulta, LineaAjusteConsultaJpa lineasConsulta) {
		this.consulta = consulta;
		this.lineasConsulta = lineasConsulta;
	}

	@Override
	public List<Ajuste> listar() {
		return consulta.findAll().stream().map(this::aDominio).toList();
	}

	@Override
	public Ajuste obtener(Long id) {
		return aDominio(consulta.findById(id)
				.orElseThrow(() -> new NoEncontradoExcepcion("Ajuste no encontrado")));
	}

	@Override
	@Transactional
	public Ajuste guardar(Ajuste ajuste) {
		EntidadAjuste entidad;
		if (ajuste.getId() != null) {
			entidad = consulta.findById(ajuste.getId())
					.orElseThrow(() -> new NoEncontradoExcepcion("Ajuste no encontrado"));
			entidad.setFecha(ajuste.getFecha());
			entidad.setObservacion(ajuste.getObservacion());
			entidad.setMotivo(ajuste.getMotivo());
			entidad.getLineas().clear();
		} else {
			entidad = new EntidadAjuste();
			entidad.setFecha(ajuste.getFecha());
			entidad.setObservacion(ajuste.getObservacion());
			entidad.setMotivo(ajuste.getMotivo());
		}
		List<EntidadLineaAjuste> lineas = new ArrayList<>();
		if (ajuste.getLineas() != null) {
			for (LineaAjuste linea : ajuste.getLineas()) {
				EntidadLineaAjuste el = Mapeador.aEntidad(linea, entidad);
				lineas.add(el);
			}
		}
		entidad.getLineas().addAll(lineas);
		entidad = consulta.save(entidad);
		return aDominio(entidad);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		if (!consulta.existsById(id)) {
			throw new NoEncontradoExcepcion("Ajuste no encontrado");
		}
		consulta.deleteById(id);
	}

	@Override
	public boolean tieneProducto(String tipoProducto, Long productoId) {
		return lineasConsulta.existsByTipoProductoAndProductoId(tipoProducto, productoId);
	}

	private Ajuste aDominio(EntidadAjuste e) {
		return Mapeador.aDominio(e, Mapeador.aDominioLineasAjuste(e.getLineas()));
	}
}
