package com.art.inventario.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.Proveedor;
import com.art.inventario.excepcion.ConflictoExcepcion;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.puerto.entrada.ProveedorCasoDeUso;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ProveedorPersistencia;

@Service
public class ProveedorAplicacion implements ProveedorCasoDeUso {

	private final ProveedorPersistencia persistencia;
	private final CambiosNotificador notificador;

	public ProveedorAplicacion(ProveedorPersistencia persistencia, CambiosNotificador notificador) {
		this.persistencia = persistencia;
		this.notificador = notificador;
	}

	@Override
	public List<Proveedor> listar() {
		return persistencia.listar();
	}

	@Override
	public PaginaResultado<Proveedor> listarPagina(int pagina, int tamano) {
		return persistencia.listarPagina(PaginaResultado.paginaSegura(pagina), PaginaResultado.tamanoSeguro(tamano));
	}

	@Override
	public Proveedor obtener(Long id) {
		return persistencia.obtener(id);
	}

	@Override
	@Transactional
	public Proveedor crear(Proveedor proveedor) {
		validarNombre(proveedor);
		if (persistencia.existeNombre(proveedor.getNombre(), null)) {
			throw new DatosInvalidosExcepcion("Ya existe un proveedor con ese nombre");
		}
		Proveedor creado = persistencia.guardar(proveedor);
		notificador.publicar(CambiosNotificador.RECURSO_PROVEEDORES);
		return creado;
	}

	@Override
	@Transactional
	public Proveedor actualizar(Long id, Proveedor datos) {
		persistencia.obtener(id);
		validarNombre(datos);
		if (persistencia.existeNombre(datos.getNombre(), id)) {
			throw new DatosInvalidosExcepcion("Ya existe un proveedor con ese nombre");
		}
		datos.setId(id);
		Proveedor guardado = persistencia.guardar(datos);
		notificador.publicar(CambiosNotificador.RECURSO_PROVEEDORES);
		return guardado;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		persistencia.obtener(id);
		if (persistencia.tieneComprasOFacturas(id)) {
			throw new ConflictoExcepcion("No se puede eliminar: el proveedor tiene compras o facturas asociadas");
		}
		persistencia.eliminar(id);
		notificador.publicar(CambiosNotificador.RECURSO_PROVEEDORES);
	}

	private void validarNombre(Proveedor proveedor) {
		if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
			throw new DatosInvalidosExcepcion("El nombre es obligatorio");
		}
	}
}