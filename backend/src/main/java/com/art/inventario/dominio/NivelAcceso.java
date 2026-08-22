package com.art.inventario.dominio;

public final class NivelAcceso {

	private Long id;
	private String codigo;
	private String nombre;
	private Long usuarioRaizId;

	public NivelAcceso() {
	}

	public NivelAcceso(String codigo, String nombre) {
		this.codigo = codigo;
		this.nombre = nombre;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Long getUsuarioRaizId() {
		return usuarioRaizId;
	}

	public void setUsuarioRaizId(Long usuarioRaizId) {
		this.usuarioRaizId = usuarioRaizId;
	}

	public boolean esRaiz() {
		return "ROOT".equalsIgnoreCase(codigo);
	}
}