package com.art.inventario.persistencia.adaptador;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.dominio.Compra;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.persistencia.consulta.ConsumibleConsultaJpa;
import com.art.inventario.persistencia.consulta.EppConsultaJpa;
import com.art.inventario.persistencia.consulta.HerramientaConsultaJpa;
import com.art.inventario.persistencia.consulta.MaterialConsultaJpa;
import com.art.inventario.puerto.salida.ProductoCostoPersistencia;

@Repository
public class ProductoCostoPersistenciaJpa implements ProductoCostoPersistencia {

	private final HerramientaConsultaJpa herramientas;
	private final EppConsultaJpa epps;
	private final ConsumibleConsultaJpa consumibles;
	private final MaterialConsultaJpa materiales;

	public ProductoCostoPersistenciaJpa(HerramientaConsultaJpa herramientas, EppConsultaJpa epps,
			ConsumibleConsultaJpa consumibles, MaterialConsultaJpa materiales) {
		this.herramientas = herramientas;
		this.epps = epps;
		this.consumibles = consumibles;
		this.materiales = materiales;
	}

	@Override
	@Transactional
	public void actualizarUltimoCosto(String tipo, Long productoId, Double costo) {
		switch (tipo) {
		case Compra.TIPO_HERRAMIENTA -> herramientas.findById(productoId).ifPresentOrElse(
				h -> {
					h.setUltimoCosto(costo);
					herramientas.save(h);
				},
				() -> { throw new NoEncontradoExcepcion("Herramienta no encontrada"); });
		case Compra.TIPO_EPP -> epps.findById(productoId).ifPresentOrElse(
				e -> {
					e.setUltimoCosto(costo);
					epps.save(e);
				},
				() -> { throw new NoEncontradoExcepcion("EPP no encontrado"); });
		case Compra.TIPO_CONSUMIBLE -> consumibles.findById(productoId).ifPresentOrElse(
				c -> {
					c.setUltimoCosto(costo);
					consumibles.save(c);
				},
				() -> { throw new NoEncontradoExcepcion("Consumible no encontrado"); });
		case Compra.TIPO_MATERIAL -> materiales.findById(productoId).ifPresentOrElse(
				m -> {
					m.setUltimoCosto(costo);
					materiales.save(m);
				},
				() -> { throw new NoEncontradoExcepcion("Material no encontrado"); });
		case Compra.TIPO_ROPA -> {
			// la ropa no lleva costo por producto
		}
		default -> throw new DatosInvalidosExcepcion("Tipo de producto no válido: " + tipo);
		}
	}
}