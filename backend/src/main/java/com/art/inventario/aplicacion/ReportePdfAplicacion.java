package com.art.inventario.aplicacion;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.art.inventario.dominio.AlertaReposicion;
import com.art.inventario.dominio.Consumible;
import com.art.inventario.dominio.Epp;
import com.art.inventario.dominio.Factura;
import com.art.inventario.dominio.Herramienta;
import com.art.inventario.dominio.Material;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.AlertaCasoDeUso;
import com.art.inventario.puerto.entrada.HerramientaCasoDeUso;
import com.art.inventario.puerto.entrada.ReportePdfCasoDeUso;
import com.art.inventario.puerto.salida.ConsumiblePersistencia;
import com.art.inventario.puerto.salida.EppPersistencia;
import com.art.inventario.puerto.salida.FacturaPersistencia;
import com.art.inventario.puerto.salida.MaterialPersistencia;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportePdfAplicacion implements ReportePdfCasoDeUso {

	private final MaterialPersistencia materiales;
	private final ConsumiblePersistencia consumibles;
	private final EppPersistencia epps;
	private final HerramientaCasoDeUso herramientas;
	private final FacturaPersistencia facturas;
	private final AlertaCasoDeUso alertas;

	public ReportePdfAplicacion(MaterialPersistencia materiales, ConsumiblePersistencia consumibles,
			EppPersistencia epps, HerramientaCasoDeUso herramientas, FacturaPersistencia facturas,
			AlertaCasoDeUso alertas) {
		this.materiales = materiales;
		this.consumibles = consumibles;
		this.epps = epps;
		this.herramientas = herramientas;
		this.facturas = facturas;
		this.alertas = alertas;
	}

	@Override
	public byte[] inventario() {
		return conDocumento("Inventario", doc -> {
			tabla(doc, new String[] { "Tipo", "Nombre", "Marca", "Stock", "Costo", "Valor" }, filas -> {
				for (Material m : materiales.listar()) {
					filas.add(filaInv("MATERIAL", m.getNombre(), m.getMarca(), m.getStock(), m.getUltimoCosto()));
				}
				for (Consumible c : consumibles.listar()) {
					filas.add(filaInv("CONSUMIBLE", c.getNombre(), c.getMarca(), c.getStock() == null ? null : c.getStock().intValue(), c.getUltimoCosto()));
				}
				for (Epp e : epps.listar()) {
					filas.add(filaInv("EPP", e.getNombre(), e.getMarca(), e.getStock(), e.getUltimoCosto()));
				}
				for (Herramienta h : herramientas.listar()) {
					filas.add(filaInv("HERRAMIENTA", h.getNombre(), h.getMarca(), h.getCantidadTotal(), h.getUltimoCosto()));
				}
			});
		});
	}

	@Override
	public byte[] facturas(String desde, String hasta) {
		return conDocumento("Facturas", doc -> {
			tabla(doc, new String[] { "Fecha", "Número", "Proveedor", "Total" }, filas -> {
				for (Factura f : facturas.listar()) {
					if (!enRango(f.getFecha(), desde, hasta)) {
						continue;
					}
					filas.add(new String[] {
							n(f.getFecha()),
							n(f.getNumero()),
							f.getProveedor() == null ? "" : n(f.getProveedor().getNombre()),
							String.valueOf(f.getTotal() == null ? 0 : f.getTotal())
					});
				}
			});
		});
	}

	@Override
	public byte[] valorInventario() {
		return conDocumento("Valor del inventario", doc -> {
			tabla(doc, new String[] { "Tipo", "Nombre", "Stock", "Costo unit.", "Valor" }, filas -> {
				double total = 0;
				for (Material m : materiales.listar()) {
					String[] f = filaValor("MATERIAL", m.getNombre(), m.getStock(), m.getUltimoCosto());
					total += parseValor(f[4]);
					filas.add(f);
				}
				for (Consumible c : consumibles.listar()) {
					String[] f = filaValor("CONSUMIBLE", c.getNombre(), c.getStock() == null ? null : c.getStock().intValue(), c.getUltimoCosto());
					total += parseValor(f[4]);
					filas.add(f);
				}
				for (Epp e : epps.listar()) {
					String[] f = filaValor("EPP", e.getNombre(), e.getStock(), e.getUltimoCosto());
					total += parseValor(f[4]);
					filas.add(f);
				}
				for (Herramienta h : herramientas.listar()) {
					String[] f = filaValor("HERRAMIENTA", h.getNombre(), h.getCantidadTotal(), h.getUltimoCosto());
					total += parseValor(f[4]);
					filas.add(f);
				}
				filas.add(new String[] { "", "TOTAL", "", "", String.format("%.0f", total) });
			});
		});
	}

	@Override
	public byte[] alertasReposicion() {
		return conDocumento("Alertas de reposición", doc -> {
			tabla(doc, new String[] { "Tipo", "Producto", "Marca", "Stock", "Mínimo" }, filas -> {
				for (AlertaReposicion a : alertas.listarReposicion()) {
					filas.add(new String[] {
							n(a.getTipo()), n(a.getNombre()), n(a.getMarca()),
							String.valueOf(a.getStock()), String.valueOf(a.getStockMinimo())
					});
				}
			});
		});
	}

	@FunctionalInterface
	private interface DocWriter {
		void write(Document doc) throws Exception;
	}

	@FunctionalInterface
	private interface FilasWriter {
		void write(List<String[]> filas) throws Exception;
	}

	private byte[] conDocumento(String titulo, DocWriter writer) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
			PdfWriter.getInstance(doc, baos);
			doc.open();
			Font h = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
			Paragraph p = new Paragraph(titulo, h);
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(12);
			doc.add(p);
			writer.write(doc);
			doc.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new DatosInvalidosExcepcion("No se pudo generar el PDF: " + e.getMessage());
		}
	}

	private void tabla(Document doc, String[] headers, FilasWriter writer) throws Exception {
		PdfPTable table = new PdfPTable(headers.length);
		table.setWidthPercentage(100);
		Font hf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
		Font cf = FontFactory.getFont(FontFactory.HELVETICA, 8);
		for (String h : headers) {
			PdfPCell cell = new PdfPCell(new Phrase(h, hf));
			table.addCell(cell);
		}
		java.util.ArrayList<String[]> filas = new java.util.ArrayList<>();
		writer.write(filas);
		for (String[] fila : filas) {
			for (String c : fila) {
				table.addCell(new Phrase(c == null ? "" : c, cf));
			}
		}
		doc.add(table);
	}

	private static String[] filaInv(String tipo, String nombre, String marca, Integer stock, Double costo) {
		int st = stock == null ? 0 : stock;
		double c = costo == null ? 0 : costo;
		return new String[] { tipo, n(nombre), n(marca), String.valueOf(st),
				costo == null ? "" : String.format("%.0f", c),
				costo == null ? "" : String.format("%.0f", st * c) };
	}

	private static String[] filaValor(String tipo, String nombre, Integer stock, Double costo) {
		int st = stock == null ? 0 : stock;
		double c = costo == null ? 0 : costo;
		return new String[] { tipo, n(nombre), String.valueOf(st),
				costo == null ? "" : String.format("%.0f", c),
				costo == null ? "0" : String.format("%.0f", st * c) };
	}

	private static double parseValor(String v) {
		try {
			return Double.parseDouble(v.replace(",", ""));
		} catch (Exception e) {
			return 0;
		}
	}

	private static boolean enRango(String fecha, String desde, String hasta) {
		if (fecha == null) {
			return false;
		}
		if (desde != null && !desde.isBlank() && fecha.compareTo(desde) < 0) {
			return false;
		}
		if (hasta != null && !hasta.isBlank() && fecha.compareTo(hasta) > 0) {
			return false;
		}
		return true;
	}

	private static String n(String s) {
		return s == null ? "" : s;
	}
}
