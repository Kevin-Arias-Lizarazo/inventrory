package com.art.inventario.aplicacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.Proveedor;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ConsumibleCasoDeUso;
import com.art.inventario.puerto.entrada.EppCasoDeUso;
import com.art.inventario.puerto.entrada.ImportacionCasoDeUso;
import com.art.inventario.puerto.entrada.MaterialCasoDeUso;
import com.art.inventario.puerto.entrada.ProveedorCasoDeUso;

@Service
public class ImportacionAplicacion implements ImportacionCasoDeUso {
	private final ProveedorCasoDeUso proveedores;
	private final MaterialCasoDeUso materiales;
	private final ConsumibleCasoDeUso consumibles;
	private final EppCasoDeUso epps;

	public ImportacionAplicacion(ProveedorCasoDeUso proveedores, MaterialCasoDeUso materiales,
			ConsumibleCasoDeUso consumibles, EppCasoDeUso epps) {
		this.proveedores = proveedores;
		this.materiales = materiales;
		this.consumibles = consumibles;
		this.epps = epps;
	}

	@Override
	@Transactional
	public Map<String, Object> importarCsv(String recurso, String csv) {
		if (csv == null || csv.isBlank()) {
			throw new DatosInvalidosExcepcion("CSV vacío");
		}
		String[] lineas = csv.replace("\r\n", "\n").replace('\r', '\n').split("\n");
		if (lineas.length < 2) {
			throw new DatosInvalidosExcepcion("El CSV debe tener encabezado y al menos una fila");
		}
		String[] headers = splitCsv(lineas[0]);
		Map<String, Integer> idx = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			idx.put(headers[i].trim().toLowerCase(Locale.ROOT), i);
		}
		int creados = 0;
		List<String> errores = new ArrayList<>();
		String rec = recurso == null ? "" : recurso.toLowerCase(Locale.ROOT);
		for (int i = 1; i < lineas.length; i++) {
			if (lineas[i].isBlank()) continue;
			String[] cols = splitCsv(lineas[i]);
			try {
				switch (rec) {
				case "proveedores" -> {
					Proveedor p = new Proveedor();
					p.setNombre(celda(cols, idx, "nombre"));
					p.setTelefono(celda(cols, idx, "telefono"));
					p.setCorreo(celda(cols, idx, "correo"));
					p.setDireccion(celda(cols, idx, "direccion"));
					proveedores.crear(p);
					creados++;
				}
				case "materiales" -> {
					Material m = new Material();
					m.setNombre(celda(cols, idx, "nombre"));
					m.setMarca(celda(cols, idx, "marca"));
					m.setUnidad(celda(cols, idx, "unidad"));
					m.setDescripcion(celda(cols, idx, "descripcion"));
					m.setStock(0);
					materiales.crear(m);
					creados++;
				}
				case "consumibles" -> {
					Consumible c = new Consumible();
					c.setNombre(celda(cols, idx, "nombre"));
					c.setMarca(celda(cols, idx, "marca"));
					c.setUnidad(celda(cols, idx, "unidad"));
					c.setDescripcion(celda(cols, idx, "descripcion"));
					c.setStock(0);
					consumibles.crear(c);
					creados++;
				}
				case "epp" -> {
					Epp e = new Epp();
					e.setNombre(celda(cols, idx, "nombre"));
					e.setMarca(celda(cols, idx, "marca"));
					e.setDescripcion(celda(cols, idx, "descripcion"));
					e.setStock(0);
					epps.crear(e);
					creados++;
				}
				default -> throw new DatosInvalidosExcepcion("Recurso no soportado: " + recurso);
				}
			} catch (RuntimeException ex) {
				errores.add("Fila " + (i + 1) + ": " + ex.getMessage());
			}
		}
		Map<String, Object> out = new HashMap<>();
		out.put("creados", creados);
		out.put("errores", errores);
		return out;
	}

	private static String celda(String[] cols, Map<String, Integer> idx, String key) {
		Integer i = idx.get(key);
		if (i == null || i >= cols.length) return null;
		String v = cols[i].trim();
		return v.isEmpty() ? null : v;
	}

	private static String[] splitCsv(String line) {
		List<String> out = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean q = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') { q = !q; continue; }
			if (c == ',' && !q) { out.add(cur.toString()); cur.setLength(0); continue; }
			cur.append(c);
		}
		out.add(cur.toString());
		return out.toArray(String[]::new);
	}
}
