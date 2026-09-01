package com.art.inventario.aplicacion.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Consulta de listado paginado y filtrable. Agrupa la paginaci&oacute;n, la
 * b&uacute;squeda libre, el orden y los filtros por campos.
 */
public class ConsultaPaginada {

	public static final String CLAVE_PAGINA = "pagina";
	public static final String CLAVE_TAMANO = "tamano";
	public static final String CLAVE_Q = "q";
	public static final String CLAVE_ORDEN = "orden";
	public static final String CLAVE_DIR = "dir";

	private int pagina;
	private int tamano;
	private String q;
	private String orden;
	private String dir;
	private Map<String, String> filtros;

	public ConsultaPaginada() {
		this.pagina = 0;
		this.tamano = PaginaResultado.tamanoSeguro(null);
		this.filtros = new HashMap<>();
	}

	/**
	 * Construye la consulta a partir de los par&aacute;metros HTTP. Las claves
	 * reservadas (pagina, tamano, q, orden, dir) se interpretan; el resto se
	 * tratan como filtros por campo.
	 */
	public static ConsultaPaginada desdeParams(Map<String, String> params) {
		ConsultaPaginada c = new ConsultaPaginada();
		c.pagina = PaginaResultado.paginaSegura(entero(params.get(CLAVE_PAGINA)));
		c.tamano = PaginaResultado.tamanoSeguro(entero(params.get(CLAVE_TAMANO)));
		c.q = valor(params.get(CLAVE_Q));
		c.orden = valor(params.get(CLAVE_ORDEN));
		c.dir = valor(params.get(CLAVE_DIR));
		c.filtros = new HashMap<>();
		for (Map.Entry<String, String> e : params.entrySet()) {
			String clave = e.getKey();
			if (esClaveReservada(clave)) {
				continue;
			}
			c.filtros.put(clave, valor(e.getValue()));
		}
		return c;
	}

	private static boolean esClaveReservada(String clave) {
		return CLAVE_PAGINA.equals(clave) || CLAVE_TAMANO.equals(clave)
				|| CLAVE_Q.equals(clave) || CLAVE_ORDEN.equals(clave)
				|| CLAVE_DIR.equals(clave);
	}

	private static Integer entero(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(valor);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String valor(String v) {
		return v == null ? null : v;
	}

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public int getTamano() {
		return tamano;
	}

	public void setTamano(int tamano) {
		this.tamano = tamano;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getOrden() {
		return orden;
	}

	public void setOrden(String orden) {
		this.orden = orden;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public Map<String, String> getFiltros() {
		return filtros;
	}

	public void setFiltros(Map<String, String> filtros) {
		this.filtros = filtros;
	}

	/**
	 * Devuelve una copia de la consulta con los mismos valores, compartiendo el
	 * mapa de filtros. &Uacute;til para forzar un filtro (p. ej. el recursoId de
	 * una ruta /{id}/movimientos/paginado) sin mutar la consulta original.
	 */
	public ConsultaPaginada conCopy() {
		ConsultaPaginada copia = new ConsultaPaginada();
		copia.pagina = this.pagina;
		copia.tamano = this.tamano;
		copia.q = this.q;
		copia.orden = this.orden;
		copia.dir = this.dir;
		copia.filtros = new HashMap<>(this.filtros);
		return copia;
	}
}
