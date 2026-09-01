package com.art.inventario.puerto.salida;
import java.util.List;
import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.OrdenCompra;
public interface OrdenCompraPersistencia {
	List<OrdenCompra> listar();
	PaginaResultado<OrdenCompra> listarPagina(ConsultaPaginada consultaPaginada);
	OrdenCompra obtener(Long id);
	OrdenCompra guardar(OrdenCompra orden);
	void eliminar(Long id);
}
