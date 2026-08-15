package com.art.inventario.dominio;

public final class Herramienta {

	private Long id;
	private String nombre;
	private String marca;
	private String codigo;
	private String descripcion;
	private String fotoUrl;
	private Integer cantidadTotal;
	private Integer cantidadDanada;
	private Integer cantidadPerdida;
	private Integer cantidadAsignada;
	private Integer cantidadDisponible;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFotoUrl() {
		return fotoUrl;
	}

	public void setFotoUrl(String fotoUrl) {
		this.fotoUrl = fotoUrl;
	}

	public Integer getCantidadTotal() {
		return cantidadTotal;
	}

	public void setCantidadTotal(Integer cantidadTotal) {
		this.cantidadTotal = cantidadTotal;
	}

	public Integer getCantidadDanada() {
		return cantidadDanada;
	}

	public void setCantidadDanada(Integer cantidadDanada) {
		this.cantidadDanada = cantidadDanada;
	}

	public Integer getCantidadPerdida() {
		return cantidadPerdida;
	}

	public void setCantidadPerdida(Integer cantidadPerdida) {
		this.cantidadPerdida = cantidadPerdida;
	}

	public Integer getCantidadAsignada() {
		return cantidadAsignada;
	}

	public void setCantidadAsignada(Integer cantidadAsignada) {
		this.cantidadAsignada = cantidadAsignada;
	}

	public Integer getCantidadDisponible() {
		return cantidadDisponible;
	}

	public void setCantidadDisponible(Integer cantidadDisponible) {
		this.cantidadDisponible = cantidadDisponible;
	}
}