package com.art.inventario.aplicacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.art.inventario.dominio.Compra;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Material;
import com.art.inventario.dominio.ResumenDashboard;
import com.art.inventario.puerto.entrada.AlertaCasoDeUso;
import com.art.inventario.puerto.entrada.DashboardCasoDeUso;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.salida.CompraPersistencia;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;
import com.art.inventario.puerto.salida.FacturaPersistencia;
import com.art.inventario.puerto.salida.MaterialPersistencia;

@Service
public class DashboardAplicacion implements DashboardCasoDeUso {
	private final MaterialPersistencia materiales;
	private final ConsumiblePersistencia consumibles;
	private final EppPersistencia epps;
	private final HerramientaCasoDeUso herramientas;
	private final FacturaPersistencia facturas;
	private final CompraPersistencia compras;
	private final AlertaCasoDeUso alertas;

	public DashboardAplicacion(MaterialPersistencia materiales, ConsumiblePersistencia consumibles,
			EppPersistencia epps, HerramientaCasoDeUso herramientas, FacturaPersistencia facturas,
			CompraPersistencia compras, AlertaCasoDeUso alertas) {
		this.materiales = materiales;
		this.consumibles = consumibles;
		this.epps = epps;
		this.herramientas = herramientas;
		this.facturas = facturas;
		this.compras = compras;
		this.alertas = alertas;
	}

	@Override
	public ResumenDashboard resumen(String desde, String hasta) {
		ResumenDashboard r = new ResumenDashboard();
		double valor = 0;
		int sinCosto = 0;
		int total = 0;
		List<Map<String, Object>> porCat = new ArrayList<>();

		double vMat = 0; int nMat = 0; int sMat = 0;
		for (Material m : materiales.listar()) {
			total++; nMat++;
			int st = m.getStock() == null ? 0 : m.getStock();
			if (m.getUltimoCosto() == null) { sinCosto += st > 0 ? 1 : 0; }
			else { vMat += st * m.getUltimoCosto(); }
		}
		valor += vMat;
		porCat.add(cat("MATERIAL", nMat, vMat));

		double vCon = 0; int nCon = 0;
		for (Consumible c : consumibles.listar()) {
			total++; nCon++;
			int st = c.getStock() == null ? 0 : c.getStock();
			if (c.getUltimoCosto() == null) { if (st > 0) sinCosto++; }
			else { vCon += st * c.getUltimoCosto(); }
		}
		valor += vCon;
		porCat.add(cat("CONSUMIBLE", nCon, vCon));

		double vEpp = 0; int nEpp = 0;
		for (Epp e : epps.listar()) {
			total++; nEpp++;
			int st = e.getStock() == null ? 0 : e.getStock();
			if (e.getUltimoCosto() == null) { if (st > 0) sinCosto++; }
			else { vEpp += st * e.getUltimoCosto(); }
		}
		valor += vEpp;
		porCat.add(cat("EPP", nEpp, vEpp));

		double vHer = 0; int nHer = 0;
		for (Herramienta h : herramientas.listar()) {
			total++; nHer++;
			int st = h.getCantidadTotal() == null ? 0 : h.getCantidadTotal();
			if (h.getUltimoCosto() == null) { if (st > 0) sinCosto++; }
			else { vHer += st * h.getUltimoCosto(); }
		}
		valor += vHer;
		porCat.add(cat("HERRAMIENTA", nHer, vHer));

		double gasto = 0;
		for (Factura f : facturas.listar()) {
			if (enRango(f.getFecha(), desde, hasta)) {
				gasto += f.getTotal() == null ? 0 : f.getTotal();
			}
		}
		int comprasN = 0;
		for (Compra c : compras.listar()) {
			if (enRango(c.getFecha(), desde, hasta)) comprasN++;
		}

		r.setValorInventario(valor);
		r.setProductosSinCosto(sinCosto);
		r.setTotalProductos(total);
		r.setValorPorCategoria(porCat);
		r.setGastoFacturasRango(gasto);
		r.setComprasRango(comprasN);
		r.setAlertasReposicion(alertas.listarReposicion().size());
		r.setAlertasVencimientoEpp(alertas.listarVencimientoEpp(30).size());
		return r;
	}

	private static Map<String, Object> cat(String tipo, int cantidad, double valor) {
		Map<String, Object> m = new HashMap<>();
		m.put("tipo", tipo);
		m.put("cantidad", cantidad);
		m.put("valor", valor);
		return m;
	}

	private static boolean enRango(String fecha, String desde, String hasta) {
		if (fecha == null) return false;
		if (desde != null && !desde.isBlank() && fecha.compareTo(desde) < 0) return false;
		if (hasta != null && !hasta.isBlank() && fecha.compareTo(hasta) > 0) return false;
		return true;
	}
}
