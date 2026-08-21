package com.art.inventario.dominio;

public final class Sesion {

	private Long id;
	private String accessHash;
	private String refreshHash;
	private String accessVence;
	private Long usuarioId;
	private String username;
	private Rol rol;
	private String permisos;
	private String fechaCreacion;
	private String fechaFin;
	private Boolean bloqueada;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccessHash() {
		return accessHash;
	}

	public void setAccessHash(String accessHash) {
		this.accessHash = accessHash;
	}

	public String getRefreshHash() {
		return refreshHash;
	}

	public void setRefreshHash(String refreshHash) {
		this.refreshHash = refreshHash;
	}

	public String getAccessVence() {
		return accessVence;
	}

	public void setAccessVence(String accessVence) {
		this.accessVence = accessVence;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public String getPermisos() {
		return permisos;
	}

	public void setPermisos(String permisos) {
		this.permisos = permisos;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Boolean getBloqueada() {
		return bloqueada;
	}

	public void setBloqueada(Boolean bloqueada) {
		this.bloqueada = bloqueada;
	}

	public boolean bloqueada() {
		return Boolean.TRUE.equals(bloqueada);
	}
}