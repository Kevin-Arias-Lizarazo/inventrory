package com.art.inventario.persistencia;

import java.util.List;
import java.util.stream.Collectors;

import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.dominio.EntregaRopa;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.Minuta;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.persistencia.entidad.EntidadAsignacionConsumible;
import com.art.inventario.persistencia.entidad.EntidadAsignacionHerramienta;
import com.art.inventario.persistencia.entidad.EntidadConsumible;
import com.art.inventario.persistencia.entidad.EntidadContrato;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadEntregaEpp;
import com.art.inventario.persistencia.entidad.EntidadEntregaRopa;
import com.art.inventario.persistencia.entidad.EntidadEpp;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.persistencia.entidad.EntidadMaterial;
import com.art.inventario.persistencia.entidad.EntidadMinuta;
import com.art.inventario.persistencia.entidad.EntidadMovimientoConsumible;
import com.art.inventario.persistencia.entidad.EntidadMovimientoHerramienta;
import com.art.inventario.persistencia.entidad.EntidadMovimientoMaterial;
import com.art.inventario.persistencia.entidad.EntidadProyecto;

public final class Mapeador {

	private Mapeador() {
	}

	public static EntidadEmpleado aEntidad(Empleado e) {
		if (e == null) {
			return null;
		}
		EntidadEmpleado ee = new EntidadEmpleado();
		ee.setId(e.getId());
		ee.setCodigo(e.getCodigo());
		ee.setNombre(e.getNombre());
		ee.setDocumento(e.getDocumento());
		ee.setCargo(e.getCargo());
		ee.setTelefono(e.getTelefono());
		ee.setCorreo(e.getCorreo());
		ee.setDireccion(e.getDireccion());
		ee.setFechaIngreso(e.getFechaIngreso());
		ee.setHojaVida(e.getHojaVida());
		ee.setFotoUrl(e.getFotoUrl());
		return ee;
	}

	public static Empleado aDominio(EntidadEmpleado e) {
		if (e == null) {
			return null;
		}
		Empleado d = new Empleado();
		d.setId(e.getId());
		d.setCodigo(e.getCodigo());
		d.setNombre(e.getNombre());
		d.setDocumento(e.getDocumento());
		d.setCargo(e.getCargo());
		d.setTelefono(e.getTelefono());
		d.setCorreo(e.getCorreo());
		d.setDireccion(e.getDireccion());
		d.setFechaIngreso(e.getFechaIngreso());
		d.setHojaVida(e.getHojaVida());
		d.setFotoUrl(e.getFotoUrl());
		return d;
	}

	public static EntidadEmpleado soloReferencia(Long empleadoId) {
		EntidadEmpleado ee = new EntidadEmpleado();
		ee.setId(empleadoId);
		return ee;
	}

	public static List<Empleado> aDominioEmpleados(List<EntidadEmpleado> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadContrato aEntidad(Contrato c, EntidadEmpleado empleado) {
		if (c == null) {
			return null;
		}
		EntidadContrato ec = new EntidadContrato();
		ec.setId(c.getId());
		ec.setFechaInicio(c.getFechaInicio());
		ec.setFechaFin(c.getFechaFin());
		ec.setEstado(c.getEstado());
		ec.setEmpleado(empleado);
		return ec;
	}

	public static Contrato aDominio(EntidadContrato c) {
		if (c == null) {
			return null;
		}
		Contrato d = new Contrato();
		d.setId(c.getId());
		d.setFechaInicio(c.getFechaInicio());
		d.setFechaFin(c.getFechaFin());
		d.setEstado(c.getEstado());
		d.setEmpleado(aDominio(c.getEmpleado()));
		return d;
	}

	public static List<Contrato> aDominioContratos(List<EntidadContrato> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadMinuta aEntidad(Minuta m, EntidadEmpleado empleado, EntidadProyecto proyecto) {
		EntidadMinuta em = new EntidadMinuta();
		em.setId(m.getId());
		em.setHora(m.getHora());
		em.setFecha(m.getFecha());
		em.setEmpleado(empleado);
		em.setProyecto(proyecto);
		return em;
	}

	public static Minuta aDominio(EntidadMinuta m) {
		Minuta d = new Minuta();
		d.setId(m.getId());
		d.setHora(m.getHora());
		d.setFecha(m.getFecha());
		d.setEmpleado(aDominio(m.getEmpleado()));
		d.setProyecto(aDominio(m.getProyecto()));
		return d;
	}

	public static List<Minuta> aDominioMinutas(List<EntidadMinuta> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadProyecto aEntidad(Proyecto p) {
		if (p == null) {
			return null;
		}
		EntidadProyecto ep = new EntidadProyecto();
		ep.setId(p.getId());
		ep.setCodigo(p.getCodigo());
		ep.setNombre(p.getNombre());
		ep.setCliente(p.getCliente());
		ep.setUbicacion(p.getUbicacion());
		ep.setDescripcion(p.getDescripcion());
		ep.setFechaInicio(p.getFechaInicio());
		ep.setFechaFin(p.getFechaFin());
		ep.setEstado(p.getEstado());
		return ep;
	}

	public static Proyecto aDominio(EntidadProyecto p) {
		if (p == null) {
			return null;
		}
		Proyecto d = new Proyecto();
		d.setId(p.getId());
		d.setCodigo(p.getCodigo());
		d.setNombre(p.getNombre());
		d.setCliente(p.getCliente());
		d.setUbicacion(p.getUbicacion());
		d.setDescripcion(p.getDescripcion());
		d.setFechaInicio(p.getFechaInicio());
		d.setFechaFin(p.getFechaFin());
		d.setEstado(p.getEstado());
		return d;
	}

	public static List<Proyecto> aDominioProyectos(List<EntidadProyecto> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadEntregaRopa aEntidad(EntregaRopa e, EntidadEmpleado empleado) {
		EntidadEntregaRopa ee = new EntidadEntregaRopa();
		ee.setId(e.getId());
		ee.setFecha(e.getFecha());
		ee.setFotoUrl(e.getFotoUrl());
		ee.setFirmaUrl(e.getFirmaUrl());
		ee.setObservacion(e.getObservacion());
		ee.setEmpleado(empleado);
		return ee;
	}

	public static EntregaRopa aDominio(EntidadEntregaRopa e) {
		EntregaRopa d = new EntregaRopa();
		d.setId(e.getId());
		d.setFecha(e.getFecha());
		d.setFotoUrl(e.getFotoUrl());
		d.setFirmaUrl(e.getFirmaUrl());
		d.setObservacion(e.getObservacion());
		d.setEmpleado(aDominio(e.getEmpleado()));
		return d;
	}

	public static List<EntregaRopa> aDominioEntregasRopa(List<EntidadEntregaRopa> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadEntregaEpp aEntidad(EntregaEpp e, EntidadEmpleado empleado, EntidadEpp epp) {
		EntidadEntregaEpp ee = new EntidadEntregaEpp();
		ee.setId(e.getId());
		ee.setFecha(e.getFecha());
		ee.setObservacion(e.getObservacion());
		ee.setFotoUrl(e.getFotoUrl());
		ee.setFirmaUrl(e.getFirmaUrl());
		ee.setEmpleado(empleado);
		ee.setEpp(epp);
		return ee;
	}

	public static EntregaEpp aDominio(EntidadEntregaEpp e) {
		EntregaEpp d = new EntregaEpp();
		d.setId(e.getId());
		d.setFecha(e.getFecha());
		d.setObservacion(e.getObservacion());
		d.setFotoUrl(e.getFotoUrl());
		d.setFirmaUrl(e.getFirmaUrl());
		d.setEmpleado(aDominio(e.getEmpleado()));
		d.setEpp(aDominio(e.getEpp()));
		return d;
	}

	public static List<EntregaEpp> aDominioEntregasEpp(List<EntidadEntregaEpp> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadEpp aEntidad(Epp e) {
		if (e == null) {
			return null;
		}
		EntidadEpp ee = new EntidadEpp();
		ee.setId(e.getId());
		ee.setNombre(e.getNombre());
		ee.setDescripcion(e.getDescripcion());
		ee.setStock(e.getStock());
		ee.setFotoUrl(e.getFotoUrl());
		return ee;
	}

	public static Epp aDominio(EntidadEpp e) {
		if (e == null) {
			return null;
		}
		Epp d = new Epp();
		d.setId(e.getId());
		d.setNombre(e.getNombre());
		d.setDescripcion(e.getDescripcion());
		d.setStock(e.getStock());
		d.setFotoUrl(e.getFotoUrl());
		return d;
	}

	public static List<Epp> aDominioEpps(List<EntidadEpp> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadAsignacionHerramienta aEntidad(AsignacionHerramienta a, EntidadEmpleado empleado,
			EntidadHerramienta herramienta) {
		EntidadAsignacionHerramienta ea = new EntidadAsignacionHerramienta();
		ea.setId(a.getId());
		ea.setLugar(a.getLugar());
		ea.setFecha(a.getFecha());
		ea.setDevuelta(a.getDevuelta());
		ea.setFechaDevolucion(a.getFechaDevolucion());
		ea.setEmpleado(empleado);
		ea.setHerramienta(herramienta);
		return ea;
	}

	public static AsignacionHerramienta aDominio(EntidadAsignacionHerramienta a) {
		AsignacionHerramienta d = new AsignacionHerramienta();
		d.setId(a.getId());
		d.setLugar(a.getLugar());
		d.setFecha(a.getFecha());
		d.setDevuelta(a.getDevuelta());
		d.setFechaDevolucion(a.getFechaDevolucion());
		d.setEmpleado(aDominio(a.getEmpleado()));
		d.setHerramienta(aDominio(a.getHerramienta()));
		return d;
	}

	public static List<AsignacionHerramienta> aDominioAsignaciones(List<EntidadAsignacionHerramienta> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadAsignacionConsumible aEntidad(AsignacionConsumible a, EntidadConsumible consumible,
			EntidadProyecto proyecto) {
		if (a == null) {
			return null;
		}
		EntidadAsignacionConsumible ea = new EntidadAsignacionConsumible();
		ea.setId(a.getId());
		ea.setCantidad(a.getCantidad());
		ea.setFecha(a.getFecha());
		ea.setObservacion(a.getObservacion());
		ea.setConsumible(consumible);
		ea.setProyecto(proyecto);
		return ea;
	}

	public static AsignacionConsumible aDominio(EntidadAsignacionConsumible a) {
		if (a == null) {
			return null;
		}
		AsignacionConsumible d = new AsignacionConsumible();
		d.setId(a.getId());
		d.setCantidad(a.getCantidad());
		d.setFecha(a.getFecha());
		d.setObservacion(a.getObservacion());
		d.setConsumible(aDominio(a.getConsumible()));
		d.setProyecto(aDominio(a.getProyecto()));
		return d;
	}

	public static List<AsignacionConsumible> aDominioAsignacionesConsumibles(List<EntidadAsignacionConsumible> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadHerramienta aEntidad(Herramienta h) {
		if (h == null) {
			return null;
		}
		EntidadHerramienta eh = new EntidadHerramienta();
		eh.setId(h.getId());
		eh.setNombre(h.getNombre());
		eh.setMarca(h.getMarca());
		eh.setCodigo(h.getCodigo());
		eh.setDescripcion(h.getDescripcion());
		eh.setFotoUrl(h.getFotoUrl());
		eh.setCantidadTotal(h.getCantidadTotal());
		eh.setCantidadDanada(h.getCantidadDanada());
		eh.setCantidadPerdida(h.getCantidadPerdida());
		return eh;
	}

	public static Herramienta aDominio(EntidadHerramienta h) {
		if (h == null) {
			return null;
		}
		Herramienta d = new Herramienta();
		d.setId(h.getId());
		d.setNombre(h.getNombre());
		d.setMarca(h.getMarca());
		d.setCodigo(h.getCodigo());
		d.setDescripcion(h.getDescripcion());
		d.setFotoUrl(h.getFotoUrl());
		d.setCantidadTotal(h.getCantidadTotal());
		d.setCantidadDanada(h.getCantidadDanada());
		d.setCantidadPerdida(h.getCantidadPerdida());
		return d;
	}

	public static List<Herramienta> aDominioHerramientas(List<EntidadHerramienta> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadMovimientoHerramienta aEntidad(MovimientoHerramienta m, EntidadHerramienta herramienta) {
		if (m == null) {
			return null;
		}
		EntidadMovimientoHerramienta em = new EntidadMovimientoHerramienta();
		em.setId(m.getId());
		em.setTipo(m.getTipo());
		em.setCantidad(m.getCantidad());
		em.setFecha(m.getFecha());
		em.setObservacion(m.getObservacion());
		em.setHerramienta(herramienta);
		return em;
	}

	public static MovimientoHerramienta aDominio(EntidadMovimientoHerramienta m) {
		if (m == null) {
			return null;
		}
		MovimientoHerramienta d = new MovimientoHerramienta();
		d.setId(m.getId());
		d.setTipo(m.getTipo());
		d.setCantidad(m.getCantidad());
		d.setFecha(m.getFecha());
		d.setObservacion(m.getObservacion());
		d.setHerramienta(aDominio(m.getHerramienta()));
		return d;
	}

	public static List<MovimientoHerramienta> aDominioMovimientosHerramienta(List<EntidadMovimientoHerramienta> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadMaterial aEntidad(Material m) {
		if (m == null) {
			return null;
		}
		EntidadMaterial em = new EntidadMaterial();
		em.setId(m.getId());
		em.setNombre(m.getNombre());
		em.setUnidad(m.getUnidad());
		em.setStock(m.getStock());
		em.setDescripcion(m.getDescripcion());
		em.setFotoUrl(m.getFotoUrl());
		return em;
	}

	public static Material aDominio(EntidadMaterial m) {
		if (m == null) {
			return null;
		}
		Material d = new Material();
		d.setId(m.getId());
		d.setNombre(m.getNombre());
		d.setUnidad(m.getUnidad());
		d.setStock(m.getStock());
		d.setDescripcion(m.getDescripcion());
		d.setFotoUrl(m.getFotoUrl());
		return d;
	}

	public static List<Material> aDominioMateriales(List<EntidadMaterial> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadMovimientoMaterial aEntidad(MovimientoMaterial m, EntidadMaterial material) {
		EntidadMovimientoMaterial em = new EntidadMovimientoMaterial();
		em.setId(m.getId());
		em.setTipo(m.getTipo());
		em.setCantidad(m.getCantidad());
		em.setFecha(m.getFecha());
		em.setObservacion(m.getObservacion());
		em.setMaterial(material);
		return em;
	}

	public static MovimientoMaterial aDominio(EntidadMovimientoMaterial m) {
		MovimientoMaterial d = new MovimientoMaterial();
		d.setId(m.getId());
		d.setTipo(m.getTipo());
		d.setCantidad(m.getCantidad());
		d.setFecha(m.getFecha());
		d.setObservacion(m.getObservacion());
		d.setMaterial(aDominio(m.getMaterial()));
		return d;
	}

	public static List<MovimientoMaterial> aDominioMovimientosMaterial(List<EntidadMovimientoMaterial> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadConsumible aEntidad(Consumible c) {
		if (c == null) {
			return null;
		}
		EntidadConsumible ec = new EntidadConsumible();
		ec.setId(c.getId());
		ec.setCodigo(c.getCodigo());
		ec.setNombre(c.getNombre());
		ec.setUnidad(c.getUnidad());
		ec.setStock(c.getStock());
		ec.setDescripcion(c.getDescripcion());
		ec.setFotoUrl(c.getFotoUrl());
		return ec;
	}

	public static Consumible aDominio(EntidadConsumible c) {
		if (c == null) {
			return null;
		}
		Consumible d = new Consumible();
		d.setId(c.getId());
		d.setCodigo(c.getCodigo());
		d.setNombre(c.getNombre());
		d.setUnidad(c.getUnidad());
		d.setStock(c.getStock());
		d.setDescripcion(c.getDescripcion());
		d.setFotoUrl(c.getFotoUrl());
		return d;
	}

	public static List<Consumible> aDominioConsumibles(List<EntidadConsumible> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadMovimientoConsumible aEntidad(MovimientoConsumible m, EntidadConsumible consumible) {
		EntidadMovimientoConsumible em = new EntidadMovimientoConsumible();
		em.setId(m.getId());
		em.setTipo(m.getTipo());
		em.setCantidad(m.getCantidad());
		em.setFecha(m.getFecha());
		em.setObservacion(m.getObservacion());
		em.setConsumible(consumible);
		return em;
	}

	public static MovimientoConsumible aDominio(EntidadMovimientoConsumible m) {
		MovimientoConsumible d = new MovimientoConsumible();
		d.setId(m.getId());
		d.setTipo(m.getTipo());
		d.setCantidad(m.getCantidad());
		d.setFecha(m.getFecha());
		d.setObservacion(m.getObservacion());
		d.setConsumible(aDominio(m.getConsumible()));
		return d;
	}

	public static List<MovimientoConsumible> aDominioMovimientosConsumible(List<EntidadMovimientoConsumible> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}
}