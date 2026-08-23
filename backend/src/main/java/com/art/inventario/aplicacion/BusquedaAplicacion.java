package com.art.inventario.aplicacion;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.art.inventario.aplicacion.dto.ResultadoBusqueda;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Contrato;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.Minuta;
import com.art.inventario.dominio.Proveedor;
import com.art.inventario.dominio.Proyecto;
import com.art.inventario.puerto.entrada.BusquedaCasoDeUso;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.ContratoPersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;
import com.art.inventario.puerto.salida.HerramientaPersistencia;
import com.art.inventario.puerto.salida.MaterialPersistencia;
import com.art.inventario.puerto.salida.MinutaPersistencia;
import com.art.inventario.puerto.salida.ProveedorPersistencia;
import com.art.inventario.puerto.salida.ProyectoPersistencia;

@Service
public class BusquedaAplicacion implements BusquedaCasoDeUso {

	private final ProyectoPersistencia proyectos;
	private final ProveedorPersistencia proveedores;
	private final MaterialPersistencia materiales;
	private final ConsumiblePersistencia consumibles;
	private final EppPersistencia epps;
	private final HerramientaPersistencia herramientas;
	private final ContratoPersistencia contratos;
	private final MinutaPersistencia minutas;

	public BusquedaAplicacion(ProyectoPersistencia proyectos, ProveedorPersistencia proveedores,
			MaterialPersistencia materiales, ConsumiblePersistencia consumibles, EppPersistencia epps,
			HerramientaPersistencia herramientas, ContratoPersistencia contratos, MinutaPersistencia minutas) {
		this.proyectos = proyectos;
		this.proveedores = proveedores;
		this.materiales = materiales;
		this.consumibles = consumibles;
		this.epps = epps;
		this.herramientas = herramientas;
		this.contratos = contratos;
		this.minutas = minutas;
	}

	@Override
	public List<ResultadoBusqueda> buscar(String q) {
		if (q == null || q.isBlank()) {
			return List.of();
		}
		String criterio = normalizar(q);
		List<ResultadoBusqueda> resultados = new ArrayList<>();

		proyectos.listar().stream()
				.filter(p -> coincide(criterio, p.getNombre(), p.getCodigo(), p.getCliente()))
				.forEach(p -> resultados.add(new ResultadoBusqueda("proyecto", p.getId(),
						p.getNombre() + (p.getCodigo() != null ? " (" + p.getCodigo() + ")" : ""))));

		proveedores.listar().stream()
				.filter(p -> coincide(criterio, p.getNombre(), p.getTelefono(), p.getCorreo()))
				.forEach(p -> resultados.add(new ResultadoBusqueda("proveedor", p.getId(), p.getNombre())));

		materiales.listar().stream()
				.filter(m -> coincide(criterio, m.getNombre(), m.getMarca()))
				.forEach(m -> resultados.add(new ResultadoBusqueda("material", m.getId(), m.getNombre())));

		consumibles.listar().stream()
				.filter(c -> coincide(criterio, c.getNombre(), c.getCodigo(), c.getMarca()))
				.forEach(c -> resultados.add(new ResultadoBusqueda("consumible", c.getId(),
						c.getNombre() + (c.getCodigo() != null ? " (" + c.getCodigo() + ")" : ""))));

		epps.listar().stream()
				.filter(e -> coincide(criterio, e.getNombre(), e.getMarca()))
				.forEach(e -> resultados.add(new ResultadoBusqueda("epp", e.getId(), e.getNombre())));

		herramientas.listar().stream()
				.filter(h -> coincide(criterio, h.getNombre(), h.getCodigo(), h.getMarca()))
				.forEach(h -> resultados.add(new ResultadoBusqueda("herramienta", h.getId(),
						h.getNombre() + (h.getCodigo() != null ? " (" + h.getCodigo() + ")" : ""))));

		contratos.listar().stream()
				.filter(c -> c.getEmpleado() != null && coincide(criterio, c.getEmpleado().getNombre()))
				.forEach(c -> resultados.add(new ResultadoBusqueda("contrato", c.getId(),
						"Contrato de " + c.getEmpleado().getNombre())));

		minutas.listar().stream()
				.filter(m -> coincideEmpleadoProyecto(m, criterio))
				.forEach(m -> resultados.add(new ResultadoBusqueda("minuta", m.getId(), etiquetaMinuta(m))));

		return resultados;
	}

	private boolean coincideEmpleadoProyecto(Minuta m, String criterio) {
		String empleado = m.getEmpleado() == null ? "" : m.getEmpleado().getNombre();
		String proyecto = m.getProyecto() == null ? "" : m.getProyecto().getNombre();
		return coincide(criterio, empleado, proyecto);
	}

	private String etiquetaMinuta(Minuta m) {
		String empleado = m.getEmpleado() == null ? "" : m.getEmpleado().getNombre();
		String proyecto = m.getProyecto() == null ? "" : m.getProyecto().getNombre();
		String fecha = m.getFecha() == null ? "" : m.getFecha();
		return "Minuta " + empleado + (proyecto.isBlank() ? "" : " · " + proyecto) + (fecha.isBlank() ? "" : " · " + fecha);
	}

	private boolean coincide(String criterio, String... valores) {
		String c = normalizar(criterio);
		for (String v : valores) {
			if (v != null && normalizar(v).contains(c)) {
				return true;
			}
		}
		return false;
	}

	private static String normalizar(String texto) {
		if (texto == null) {
			return "";
		}
		return Normalizer.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase();
	}
}