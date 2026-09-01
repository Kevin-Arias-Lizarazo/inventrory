package com.art.inventario.aplicacion.dto;

import java.util.List;

public class PaginaResultado<T> {

	private List<T> contenido;
	private int pagina;
	private int tamano;
	private long total;
	private int totalPaginas;

	public PaginaResultado() {
	}

	public PaginaResultado(List<T> contenido, int pagina, int tamano, long total, int totalPaginas) {
		this.contenido = contenido;
		this.pagina = pagina;
		this.tamano = tamano;
		this.total = total;
		this.totalPaginas = totalPaginas;
	}

	public static int tamanoSeguro(Integer tamano) {
		if (tamano == null || tamano < 1) {
			return 50;
		}
		return Math.min(tamano, 100);
	}

	public static int paginaSegura(Integer pagina) {
		return pagina == null || pagina < 0 ? 0 : pagina;
	}

	public static <T> PaginaResultado<T> deLista(List<T> lista, Integer pagina, Integer tamano) {
		int p = paginaSegura(pagina);
		int t = tamanoSeguro(tamano);
		int desde = p * t;
		List<T> contenido = desde >= lista.size() ? List.of()
				: lista.subList(desde, Math.min(desde + t, lista.size()));
		int totalPaginas = (int) Math.ceil(lista.size() / (double) t);
		return new PaginaResultado<>(contenido, p, t, lista.size(), totalPaginas);
	}

	public List<T> getContenido() {
		return contenido;
	}

	public void setContenido(List<T> contenido) {
		this.contenido = contenido;
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

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getTotalPaginas() {
		return totalPaginas;
	}

	public void setTotalPaginas(int totalPaginas) {
		this.totalPaginas = totalPaginas;
	}
}