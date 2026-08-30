package com.art.inventario.persistencia;

import java.util.List;
import java.util.stream.Collectors;

import com.art.inventario.dominio.ContratoPrestacionCalculada;
import com.art.inventario.dominio.ContratoPrestacionExtra;
import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.dominio.Prestacion;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.dominio.TipoContratoPrestacion;
import com.art.inventario.persistencia.entidad.EntidadContratoPrestacionCalculada;
import com.art.inventario.persistencia.entidad.EntidadContratoPrestacionExtra;
import com.art.inventario.persistencia.entidad.EntidadParametroLegal;
import com.art.inventario.persistencia.entidad.EntidadPrestacion;
import com.art.inventario.persistencia.entidad.EntidadTipoContrato;
import com.art.inventario.persistencia.entidad.EntidadTipoContratoPrestacion;

/**
 * Central mapping for the Fase A catalogs (tipos de contrato, prestaciones,
 * matriz tipo-contrato-prestacion and parametros legales) plus the snapshot and
 * extra entities. Kept separate from {@link Mapeador} to avoid that class
 * growing past a reasonable size.
 */
public final class MapeadorCatalogos {

	private MapeadorCatalogos() {
	}

	// ---- TipoContrato ----

	public static EntidadTipoContrato aEntidad(TipoContrato t) {
		if (t == null) {
			return null;
		}
		EntidadTipoContrato e = new EntidadTipoContrato();
		e.setId(t.getId());
		e.setNombre(t.getNombre());
		e.setDescripcion(t.getDescripcion());
		e.setActivo(t.isActivo());
		return e;
	}

	public static TipoContrato aDominio(EntidadTipoContrato e) {
		if (e == null) {
			return null;
		}
		TipoContrato t = new TipoContrato();
		t.setId(e.getId());
		t.setNombre(e.getNombre());
		t.setDescripcion(e.getDescripcion());
		t.setActivo(e.isActivo());
		return t;
	}

	public static List<TipoContrato> aDominioTiposContrato(List<EntidadTipoContrato> lista) {
		if (lista == null) {
			return List.of();
		}
		return lista.stream().map(MapeadorCatalogos::aDominio).collect(Collectors.toList());
	}

	// ---- Prestacion ----

	public static EntidadPrestacion aEntidad(Prestacion p) {
		if (p == null) {
			return null;
		}
		EntidadPrestacion e = new EntidadPrestacion();
		e.setId(p.getId());
		e.setNombre(p.getNombre());
		e.setTipo(p.getTipo());
		e.setObligatoria(p.isObligatoria());
		e.setActivo(p.isActivo());
		return e;
	}

	public static Prestacion aDominio(EntidadPrestacion e) {
		if (e == null) {
			return null;
		}
		Prestacion p = new Prestacion();
		p.setId(e.getId());
		p.setNombre(e.getNombre());
		p.setTipo(e.getTipo());
		p.setObligatoria(e.isObligatoria());
		p.setActivo(e.isActivo());
		return p;
	}

	public static List<Prestacion> aDominioPrestaciones(List<EntidadPrestacion> lista) {
		if (lista == null) {
			return List.of();
		}
		return lista.stream().map(MapeadorCatalogos::aDominio).collect(Collectors.toList());
	}

	// ---- TipoContratoPrestacion (matriz) ----

	public static EntidadTipoContratoPrestacion aEntidad(TipoContratoPrestacion r, EntidadTipoContrato tipo,
			EntidadPrestacion prestacion) {
		if (r == null) {
			return null;
		}
		EntidadTipoContratoPrestacion e = new EntidadTipoContratoPrestacion();
		e.setId(r.getId());
		e.setTipoContrato(tipo);
		e.setPrestacion(prestacion);
		return e;
	}

	public static TipoContratoPrestacion aDominio(EntidadTipoContratoPrestacion e) {
		if (e == null) {
			return null;
		}
		TipoContratoPrestacion r = new TipoContratoPrestacion();
		r.setId(e.getId());
		r.setTipoContratoId(e.getTipoContrato() == null ? null : e.getTipoContrato().getId());
		r.setPrestacionId(e.getPrestacion() == null ? null : e.getPrestacion().getId());
		return r;
	}

	public static List<TipoContratoPrestacion> aDominioRelaciones(List<EntidadTipoContratoPrestacion> lista) {
		if (lista == null) {
			return List.of();
		}
		return lista.stream().map(MapeadorCatalogos::aDominio).collect(Collectors.toList());
	}

	// ---- ParametroLegal ----

	public static EntidadParametroLegal aEntidad(ParametroLegal p) {
		if (p == null) {
			return null;
		}
		EntidadParametroLegal e = new EntidadParametroLegal();
		e.setId(p.getId());
		e.setAnio(p.getAnio());
		e.setSmlmv(p.getSmlmv());
		e.setAuxilioTransporte(p.getAuxilioTransporte());
		e.setPorcentajeSalud(p.getPorcentajeSalud());
		e.setPorcentajePension(p.getPorcentajePension());
		e.setPorcentajeArl(p.getPorcentajeArl());
		e.setPorcentajeCaja(p.getPorcentajeCaja());
		e.setPorcentajeSena(p.getPorcentajeSena());
		e.setPorcentajeIcbf(p.getPorcentajeIcbf());
		return e;
	}

	public static ParametroLegal aDominio(EntidadParametroLegal e) {
		if (e == null) {
			return null;
		}
		ParametroLegal p = new ParametroLegal();
		p.setId(e.getId());
		p.setAnio(e.getAnio());
		p.setSmlmv(e.getSmlmv());
		p.setAuxilioTransporte(e.getAuxilioTransporte());
		p.setPorcentajeSalud(e.getPorcentajeSalud());
		p.setPorcentajePension(e.getPorcentajePension());
		p.setPorcentajeArl(e.getPorcentajeArl());
		p.setPorcentajeCaja(e.getPorcentajeCaja());
		p.setPorcentajeSena(e.getPorcentajeSena());
		p.setPorcentajeIcbf(e.getPorcentajeIcbf());
		return p;
	}

	public static List<ParametroLegal> aDominioParametros(List<EntidadParametroLegal> lista) {
		if (lista == null) {
			return List.of();
		}
		return lista.stream().map(MapeadorCatalogos::aDominio).collect(Collectors.toList());
	}

	// ---- ContratoPrestacionCalculada (snapshot) ----

	public static EntidadContratoPrestacionCalculada aEntidad(ContratoPrestacionCalculada c) {
		if (c == null) {
			return null;
		}
		EntidadContratoPrestacionCalculada e = new EntidadContratoPrestacionCalculada();
		e.setId(c.getId());
		e.setContratoId(c.getContratoId());
		e.setConcepto(c.getConcepto());
		e.setTipo(c.getTipo());
		e.setQuienPaga(c.getQuienPaga());
		e.setBase(c.getBase());
		e.setPorcentaje(c.getPorcentaje());
		e.setValorMensual(c.getValorMensual());
		e.setValorAnual(c.getValorAnual());
		e.setObligatoria(c.getObligatoria());
		e.setFechaCalculo(c.getFechaCalculo());
		return e;
	}

	public static ContratoPrestacionCalculada aDominio(EntidadContratoPrestacionCalculada e) {
		if (e == null) {
			return null;
		}
		ContratoPrestacionCalculada c = new ContratoPrestacionCalculada();
		c.setId(e.getId());
		c.setContratoId(e.getContratoId());
		c.setConcepto(e.getConcepto());
		c.setTipo(e.getTipo());
		c.setQuienPaga(e.getQuienPaga());
		c.setBase(e.getBase());
		c.setPorcentaje(e.getPorcentaje());
		c.setValorMensual(e.getValorMensual());
		c.setValorAnual(e.getValorAnual());
		c.setObligatoria(e.getObligatoria());
		c.setFechaCalculo(e.getFechaCalculo());
		return c;
	}

	public static List<ContratoPrestacionCalculada> aDominioCalculadas(
			List<EntidadContratoPrestacionCalculada> lista) {
		if (lista == null) {
			return List.of();
		}
		return lista.stream().map(MapeadorCatalogos::aDominio).collect(Collectors.toList());
	}

	// ---- ContratoPrestacionExtra ----

	public static EntidadContratoPrestacionExtra aEntidad(ContratoPrestacionExtra x) {
		if (x == null) {
			return null;
		}
		EntidadContratoPrestacionExtra e = new EntidadContratoPrestacionExtra();
		e.setId(x.getId());
		e.setContratoId(x.getContratoId());
		e.setConcepto(x.getConcepto());
		e.setTipo(x.getTipo());
		e.setValor(x.getValor());
		e.setFecha(x.getFecha());
		e.setVigenciaDesde(x.getVigenciaDesde());
		e.setVigenciaHasta(x.getVigenciaHasta());
		e.setObservacion(x.getObservacion());
		return e;
	}

	public static ContratoPrestacionExtra aDominio(EntidadContratoPrestacionExtra e) {
		if (e == null) {
			return null;
		}
		ContratoPrestacionExtra x = new ContratoPrestacionExtra();
		x.setId(e.getId());
		x.setContratoId(e.getContratoId());
		x.setConcepto(e.getConcepto());
		x.setTipo(e.getTipo());
		x.setValor(e.getValor());
		x.setFecha(e.getFecha());
		x.setVigenciaDesde(e.getVigenciaDesde());
		x.setVigenciaHasta(e.getVigenciaHasta());
		x.setObservacion(e.getObservacion());
		return x;
	}

	public static List<ContratoPrestacionExtra> aDominioExtras(List<EntidadContratoPrestacionExtra> lista) {
		if (lista == null) {
			return List.of();
		}
		return lista.stream().map(MapeadorCatalogos::aDominio).collect(Collectors.toList());
	}
}
