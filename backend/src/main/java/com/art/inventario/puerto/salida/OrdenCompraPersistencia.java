package com.art.inventario.puerto.salida;
import java.util.List;
import com.art.inventario.dominio.OrdenCompra;
public interface OrdenCompraPersistencia {
	List<OrdenCompra> listar();
	OrdenCompra obtener(Long id);
	OrdenCompra guardar(OrdenCompra orden);
	void eliminar(Long id);
}
