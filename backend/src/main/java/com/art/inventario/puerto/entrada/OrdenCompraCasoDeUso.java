package com.art.inventario.puerto.entrada;
import java.util.List;
import com.art.inventario.dominio.OrdenCompra;
public interface OrdenCompraCasoDeUso {
	List<OrdenCompra> listar();
	OrdenCompra obtener(Long id);
	OrdenCompra crear(OrdenCompra orden);
	OrdenCompra actualizar(Long id, OrdenCompra datos);
	void eliminar(Long id);
}
