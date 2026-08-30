package com.art.inventario.persistencia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.art.inventario.dominio.Ajuste;
import com.art.inventario.dominio.AsignacionConsumible;
import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.Devolucion;
import com.art.inventario.dominio.Empleado;
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.dominio.EntregaRopa;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.LineaAjuste;
import com.art.inventario.dominio.LineaCompra;
import com.art.inventario.dominio.LineaDevolucion;
import com.art.inventario.dominio.LineaFactura;
import com.art.inventario.dominio.LineaOrdenCompra;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.Minuta;
import com.art.inventario.dominio.MovimientoConsumible;
import com.art.inventario.dominio.MovimientoEpp;
import com.art.inventario.dominio.MovimientoHerramienta;
import com.art.inventario.dominio.MovimientoMaterial;
import com.art.inventario.dominio.NivelAcceso;
import com.art.inventario.dominio.OrdenCompra;
import com.art.inventario.dominio.PagoFactura;
import com.art.inventario.dominio.Proveedor;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.dominio.TipoContrato;
import com.art.inventario.dominio.Usuario;
import com.art.inventario.persistencia.entidad.EntidadAjuste;
import com.art.inventario.persistencia.entidad.EntidadAsignacionConsumible;
import com.art.inventario.persistencia.entidad.EntidadAsignacionHerramienta;
import com.art.inventario.persistencia.entidad.EntidadCompra;
import com.art.inventario.persistencia.entidad.EntidadConsumible;
import com.art.inventario.persistencia.entidad.EntidadContrato;
import com.art.inventario.persistencia.entidad.EntidadDevolucion;
import com.art.inventario.persistencia.entidad.EntidadEmpleado;
import com.art.inventario.persistencia.entidad.EntidadEntregaEpp;
import com.art.inventario.persistencia.entidad.EntidadEntregaRopa;
import com.art.inventario.persistencia.entidad.EntidadEpp;
import com.art.inventario.persistencia.entidad.EntidadFactura;
import com.art.inventario.persistencia.entidad.EntidadHerramienta;
import com.art.inventario.persistencia.entidad.EntidadLineaAjuste;
import com.art.inventario.persistencia.entidad.EntidadLineaCompra;
import com.art.inventario.persistencia.entidad.EntidadLineaDevolucion;
import com.art.inventario.persistencia.entidad.EntidadLineaFactura;
import com.art.inventario.persistencia.entidad.EntidadLineaOrdenCompra;
import com.art.inventario.persistencia.entidad.EntidadMaterial;
import com.art.inventario.persistencia.entidad.EntidadNivelAcceso;
import com.art.inventario.persistencia.entidad.EntidadOrdenCompra;
import com.art.inventario.persistencia.entidad.EntidadPagoFactura;
import com.art.inventario.persistencia.entidad.EntidadMinuta;
import com.art.inventario.persistencia.entidad.EntidadMovimientoConsumible;
import com.art.inventario.persistencia.entidad.EntidadMovimientoEpp;
import com.art.inventario.persistencia.entidad.EntidadMovimientoHerramienta;
import com.art.inventario.persistencia.entidad.EntidadMovimientoMaterial;
import com.art.inventario.persistencia.entidad.EntidadProveedor;
import com.art.inventario.persistencia.entidad.EntidadProyecto;
import com.art.inventario.persistencia.entidad.EntidadTipoContrato;
import com.art.inventario.persistencia.entidad.EntidadUsuario;

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

	public static EntidadContrato aEntidad(Contrato c, EntidadEmpleado empleado, EntidadTipoContrato tipoContrato) {
		if (c == null) {
			return null;
		}
		EntidadContrato ec = new EntidadContrato();
		ec.setId(c.getId());
		ec.setFechaInicio(c.getFechaInicio());
		ec.setFechaFin(c.getFechaFin());
		ec.setEstado(c.getEstado());
		ec.setEmpleado(empleado);
		ec.setTipoContrato(tipoContrato);
		ec.setRemuneracionMensual(c.getRemuneracionMensual());
		ec.setFaseAprendizaje(c.getFaseAprendizaje());
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
		d.setTipoContrato(MapeadorCatalogos.aDominio(c.getTipoContrato()));
		d.setRemuneracionMensual(c.getRemuneracionMensual());
		d.setFaseAprendizaje(c.getFaseAprendizaje());
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
		ee.setFechaVencimiento(e.getFechaVencimiento());
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
		d.setFechaVencimiento(e.getFechaVencimiento());
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
		ee.setMarca(e.getMarca());
		ee.setDescripcion(e.getDescripcion());
		ee.setStock(e.getStock());
		ee.setUltimoCosto(e.getUltimoCosto());
		ee.setStockMinimo(e.getStockMinimo());
		ee.setFechaVencimiento(e.getFechaVencimiento());
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
		d.setMarca(e.getMarca());
		d.setDescripcion(e.getDescripcion());
		d.setStock(e.getStock());
		d.setUltimoCosto(e.getUltimoCosto());
		d.setStockMinimo(e.getStockMinimo());
		d.setFechaVencimiento(e.getFechaVencimiento());
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
		ea.setCantidad(a.getCantidad());
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
		d.setCantidad(a.getCantidad());
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
		eh.setUltimoCosto(h.getUltimoCosto());
		eh.setStockMinimo(h.getStockMinimo());
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
		d.setUltimoCosto(h.getUltimoCosto());
		d.setStockMinimo(h.getStockMinimo());
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
		em.setMarca(m.getMarca());
		em.setUnidad(m.getUnidad());
		em.setStock(m.getStock());
		em.setUltimoCosto(m.getUltimoCosto());
		em.setStockMinimo(m.getStockMinimo());
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
		d.setMarca(m.getMarca());
		d.setUnidad(m.getUnidad());
		d.setStock(m.getStock());
		d.setUltimoCosto(m.getUltimoCosto());
		d.setStockMinimo(m.getStockMinimo());
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
		ec.setMarca(c.getMarca());
		ec.setUnidad(c.getUnidad());
		ec.setStock(c.getStock());
		ec.setUltimoCosto(c.getUltimoCosto());
		ec.setStockMinimo(c.getStockMinimo());
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
		d.setMarca(c.getMarca());
		d.setUnidad(c.getUnidad());
		d.setStock(c.getStock());
		d.setUltimoCosto(c.getUltimoCosto());
		d.setStockMinimo(c.getStockMinimo());
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

	public static EntidadProveedor aEntidad(Proveedor p) {
		if (p == null) {
			return null;
		}
		EntidadProveedor ep = new EntidadProveedor();
		ep.setId(p.getId());
		ep.setNombre(p.getNombre());
		ep.setTelefono(p.getTelefono());
		ep.setCorreo(p.getCorreo());
		ep.setDireccion(p.getDireccion());
		return ep;
	}

	public static Proveedor aDominio(EntidadProveedor p) {
		if (p == null) {
			return null;
		}
		Proveedor d = new Proveedor();
		d.setId(p.getId());
		d.setNombre(p.getNombre());
		d.setTelefono(p.getTelefono());
		d.setCorreo(p.getCorreo());
		d.setDireccion(p.getDireccion());
		return d;
	}

	public static List<Proveedor> aDominioProveedores(List<EntidadProveedor> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadLineaCompra aEntidad(LineaCompra l, EntidadCompra compra) {
		EntidadLineaCompra el = new EntidadLineaCompra();
		el.setId(l.getId());
		el.setCompra(compra);
		el.setTipo(l.getTipo());
		el.setProductoId(l.getProductoId());
		el.setDescripcion(l.getDescripcion());
		el.setCantidad(l.getCantidad());
		return el;
	}

	public static LineaCompra aDominio(EntidadLineaCompra l) {
		LineaCompra d = new LineaCompra();
		d.setId(l.getId());
		d.setTipo(l.getTipo());
		d.setProductoId(l.getProductoId());
		d.setDescripcion(l.getDescripcion());
		d.setCantidad(l.getCantidad());
		return d;
	}

	public static List<LineaCompra> aDominioLineasCompra(List<EntidadLineaCompra> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadCompra aEntidad(Compra c, EntidadProveedor proveedor) {
		if (c == null) {
			return null;
		}
		EntidadCompra ec = new EntidadCompra();
		ec.setId(c.getId());
		ec.setFecha(c.getFecha());
		ec.setObservacion(c.getObservacion());
		ec.setFacturaId(c.getFacturaId());
		ec.setProveedor(proveedor);
		return ec;
	}

	public static Compra aDominio(EntidadCompra c, List<LineaCompra> lineas) {
		if (c == null) {
			return null;
		}
		Compra d = new Compra();
		d.setId(c.getId());
		d.setFecha(c.getFecha());
		d.setObservacion(c.getObservacion());
		d.setFacturaId(c.getFacturaId());
		d.setProveedor(aDominio(c.getProveedor()));
		d.setLineas(lineas);
		return d;
	}

	public static List<Compra> aDominioCompras(List<EntidadCompra> lista, java.util.function.Function<EntidadCompra, List<LineaCompra>> lineasDe) {
		return lista.stream().map(c -> aDominio(c, lineasDe.apply(c))).collect(Collectors.toList());
	}

	public static EntidadLineaFactura aEntidad(LineaFactura l, EntidadFactura factura) {
		EntidadLineaFactura el = new EntidadLineaFactura();
		el.setId(l.getId());
		el.setFactura(factura);
		el.setTipo(l.getTipo());
		el.setProductoId(l.getProductoId());
		el.setDescripcion(l.getDescripcion());
		el.setCantidad(l.getCantidad());
		el.setCostoUnitario(l.getCostoUnitario());
		el.setSubtotal(l.getSubtotal());
		return el;
	}

	public static LineaFactura aDominio(EntidadLineaFactura l) {
		LineaFactura d = new LineaFactura();
		d.setId(l.getId());
		d.setTipo(l.getTipo());
		d.setProductoId(l.getProductoId());
		d.setDescripcion(l.getDescripcion());
		d.setCantidad(l.getCantidad());
		d.setCostoUnitario(l.getCostoUnitario());
		d.setSubtotal(l.getSubtotal());
		return d;
	}

	public static List<LineaFactura> aDominioLineasFactura(List<EntidadLineaFactura> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadFactura aEntidad(Factura f, EntidadProveedor proveedor) {
		if (f == null) {
			return null;
		}
		EntidadFactura ef = new EntidadFactura();
		ef.setId(f.getId());
		ef.setNumero(f.getNumero());
		ef.setFecha(f.getFecha());
		ef.setObservacion(f.getObservacion());
		ef.setCompraId(f.getCompraId());
		ef.setTotal(f.getTotal());
		ef.setProveedor(proveedor);
		return ef;
	}

	public static Factura aDominio(EntidadFactura f, List<LineaFactura> lineas) {
		if (f == null) {
			return null;
		}
		Factura d = new Factura();
		d.setId(f.getId());
		d.setNumero(f.getNumero());
		d.setFecha(f.getFecha());
		d.setObservacion(f.getObservacion());
		d.setCompraId(f.getCompraId());
		d.setTotal(f.getTotal());
		d.setProveedor(aDominio(f.getProveedor()));
		d.setLineas(lineas);
		return d;
	}

	public static List<Factura> aDominioFacturas(List<EntidadFactura> lista, java.util.function.Function<EntidadFactura, List<LineaFactura>> lineasDe) {
		return lista.stream().map(f -> aDominio(f, lineasDe.apply(f))).collect(Collectors.toList());
	}

	public static EntidadMovimientoEpp aEntidad(MovimientoEpp m, EntidadEpp epp) {
		if (m == null) {
			return null;
		}
		EntidadMovimientoEpp em = new EntidadMovimientoEpp();
		em.setId(m.getId());
		em.setTipo(m.getTipo());
		em.setCantidad(m.getCantidad());
		em.setFecha(m.getFecha());
		em.setObservacion(m.getObservacion());
		em.setEpp(epp);
		return em;
	}

	public static MovimientoEpp aDominio(EntidadMovimientoEpp m) {
		if (m == null) {
			return null;
		}
		MovimientoEpp d = new MovimientoEpp();
		d.setId(m.getId());
		d.setTipo(m.getTipo());
		d.setCantidad(m.getCantidad());
		d.setFecha(m.getFecha());
		d.setObservacion(m.getObservacion());
		d.setEpp(aDominio(m.getEpp()));
		return d;
	}

	public static List<MovimientoEpp> aDominioMovimientosEpp(List<EntidadMovimientoEpp> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadLineaAjuste aEntidad(LineaAjuste l, EntidadAjuste ajuste) {
		if (l == null) {
			return null;
		}
		EntidadLineaAjuste el = new EntidadLineaAjuste();
		el.setId(l.getId());
		el.setTipoMovimiento(l.getTipoMovimiento());
		el.setTipoProducto(l.getTipoProducto());
		el.setProductoId(l.getProductoId());
		el.setDescripcion(l.getDescripcion());
		el.setCantidad(l.getCantidad());
		el.setCantidadDisponible(l.getCantidadDisponible());
		el.setAjuste(ajuste);
		return el;
	}

	public static LineaAjuste aDominio(EntidadLineaAjuste l) {
		if (l == null) {
			return null;
		}
		LineaAjuste d = new LineaAjuste();
		d.setId(l.getId());
		d.setTipoMovimiento(l.getTipoMovimiento());
		d.setTipoProducto(l.getTipoProducto());
		d.setProductoId(l.getProductoId());
		d.setDescripcion(l.getDescripcion());
		d.setCantidad(l.getCantidad());
		d.setCantidadDisponible(l.getCantidadDisponible());
		return d;
	}

	public static List<LineaAjuste> aDominioLineasAjuste(List<EntidadLineaAjuste> lista) {
		if (lista == null) {
			return new ArrayList<>();
		}
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static Ajuste aDominio(EntidadAjuste e, List<LineaAjuste> lineas) {
		if (e == null) {
			return null;
		}
		Ajuste d = new Ajuste();
		d.setId(e.getId());
		d.setFecha(e.getFecha());
		d.setObservacion(e.getObservacion());
		d.setMotivo(e.getMotivo());
		d.setLineas(lineas != null ? lineas : new ArrayList<>());
		return d;
	}

	public static EntidadLineaDevolucion aEntidad(LineaDevolucion l, EntidadDevolucion devolucion) {
		if (l == null) {
			return null;
		}
		EntidadLineaDevolucion el = new EntidadLineaDevolucion();
		el.setId(l.getId());
		el.setTipo(l.getTipo());
		el.setProductoId(l.getProductoId());
		el.setDescripcion(l.getDescripcion());
		el.setCantidad(l.getCantidad());
		el.setDevolucion(devolucion);
		return el;
	}

	public static LineaDevolucion aDominio(EntidadLineaDevolucion l) {
		if (l == null) {
			return null;
		}
		LineaDevolucion d = new LineaDevolucion();
		d.setId(l.getId());
		d.setTipo(l.getTipo());
		d.setProductoId(l.getProductoId());
		d.setDescripcion(l.getDescripcion());
		d.setCantidad(l.getCantidad());
		return d;
	}

	public static List<LineaDevolucion> aDominioLineasDevolucion(List<EntidadLineaDevolucion> lista) {
		if (lista == null) {
			return new ArrayList<>();
		}
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static Devolucion aDominio(EntidadDevolucion e, List<LineaDevolucion> lineas) {
		if (e == null) {
			return null;
		}
		Devolucion d = new Devolucion();
		d.setId(e.getId());
		d.setFecha(e.getFecha());
		d.setObservacion(e.getObservacion());
		d.setCompraId(e.getCompraId());
		d.setLineas(lineas != null ? lineas : new ArrayList<>());
		return d;
	}

	public static EntidadPagoFactura aEntidad(PagoFactura p) {
		if (p == null) {
			return null;
		}
		EntidadPagoFactura e = new EntidadPagoFactura();
		e.setId(p.getId());
		e.setFacturaId(p.getFacturaId());
		e.setFecha(p.getFecha());
		e.setMonto(p.getMonto());
		e.setObservacion(p.getObservacion());
		return e;
	}

	public static PagoFactura aDominio(EntidadPagoFactura e) {
		if (e == null) {
			return null;
		}
		PagoFactura d = new PagoFactura();
		d.setId(e.getId());
		d.setFacturaId(e.getFacturaId());
		d.setFecha(e.getFecha());
		d.setMonto(e.getMonto());
		d.setObservacion(e.getObservacion());
		return d;
	}

	public static EntidadLineaOrdenCompra aEntidad(LineaOrdenCompra l, EntidadOrdenCompra orden) {
		if (l == null) {
			return null;
		}
		EntidadLineaOrdenCompra e = new EntidadLineaOrdenCompra();
		e.setId(l.getId());
		e.setTipo(l.getTipo());
		e.setProductoId(l.getProductoId());
		e.setDescripcion(l.getDescripcion());
		e.setCantidad(l.getCantidad());
		e.setCostoUnitario(l.getCostoUnitario());
		e.setSubtotal(l.getSubtotal());
		e.setOrden(orden);
		return e;
	}

	public static LineaOrdenCompra aDominio(EntidadLineaOrdenCompra l) {
		if (l == null) {
			return null;
		}
		LineaOrdenCompra d = new LineaOrdenCompra();
		d.setId(l.getId());
		d.setTipo(l.getTipo());
		d.setProductoId(l.getProductoId());
		d.setDescripcion(l.getDescripcion());
		d.setCantidad(l.getCantidad());
		d.setCostoUnitario(l.getCostoUnitario());
		d.setSubtotal(l.getSubtotal());
		return d;
	}

	public static List<LineaOrdenCompra> aDominioLineasOrden(List<EntidadLineaOrdenCompra> lista) {
		if (lista == null) {
			return new ArrayList<>();
		}
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static OrdenCompra aDominio(EntidadOrdenCompra e, List<LineaOrdenCompra> lineas) {
		if (e == null) {
			return null;
		}
		OrdenCompra d = new OrdenCompra();
		d.setId(e.getId());
		d.setFecha(e.getFecha());
		d.setObservacion(e.getObservacion());
		d.setTotal(e.getTotal());
		d.setProveedor(aDominio(e.getProveedor()));
		d.setLineas(lineas != null ? lineas : new ArrayList<>());
		return d;
	}

	public static EntidadUsuario aEntidad(Usuario u) {
		if (u == null) {
			return null;
		}
		EntidadUsuario e = new EntidadUsuario();
		e.setId(u.getId());
		e.setUsername(u.getUsername());
		e.setPasswordHash(u.getPasswordHash());
		e.setNombre(u.getNombre());
		e.setNivelAcceso(u.getNivelAcceso());
		e.setActivo(u.getActivo());
		e.setFechaCreacion(u.getFechaCreacion());
		e.setUltimoAcceso(u.getUltimoAcceso());
		e.setFechaBloqueo(u.getFechaBloqueo());
		e.setMotivoBloqueo(u.getMotivoBloqueo());
		return e;
	}

	public static Usuario aDominio(EntidadUsuario e) {
		if (e == null) {
			return null;
		}
		Usuario d = new Usuario();
		d.setId(e.getId());
		d.setUsername(e.getUsername());
		d.setPasswordHash(e.getPasswordHash());
		d.setNombre(e.getNombre());
		d.setNivelAcceso(e.getNivelAcceso());
		d.setActivo(e.getActivo());
		d.setFechaCreacion(e.getFechaCreacion());
		d.setUltimoAcceso(e.getUltimoAcceso());
		d.setFechaBloqueo(e.getFechaBloqueo());
		d.setMotivoBloqueo(e.getMotivoBloqueo());
		return d;
	}

	public static List<Usuario> aDominioUsuarios(List<EntidadUsuario> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}

	public static EntidadNivelAcceso aEntidad(NivelAcceso n) {
		if (n == null) {
			return null;
		}
		EntidadNivelAcceso e = new EntidadNivelAcceso();
		e.setId(n.getId());
		e.setCodigo(n.getCodigo());
		e.setNombre(n.getNombre());
		e.setUsuarioRaizId(n.getUsuarioRaizId());
		return e;
	}

	public static NivelAcceso aDominio(EntidadNivelAcceso e) {
		if (e == null) {
			return null;
		}
		NivelAcceso d = new NivelAcceso();
		d.setId(e.getId());
		d.setCodigo(e.getCodigo());
		d.setNombre(e.getNombre());
		d.setUsuarioRaizId(e.getUsuarioRaizId());
		return d;
	}

	public static List<NivelAcceso> aDominioNiveles(List<EntidadNivelAcceso> lista) {
		return lista.stream().map(Mapeador::aDominio).collect(Collectors.toList());
	}
}
