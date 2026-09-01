package com.art.inventario.puerto.salida;

import java.util.List;
import java.util.Map;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.aplicacion.dto.PaginaResultado;
import com.art.inventario.dominio.AsignacionHerramienta;

public interface AsignacionHerramientaPersistencia {

	List<AsignacionHerramienta> listar();

	PaginaResultado<AsignacionHerramienta> listarPagina(int pagina, int tamano);

	PaginaResultado<AsignacionHerramienta> listarPagina(ConsultaPaginada consultaPaginada);

	AsignacionHerramienta obtener(Long id);

	AsignacionHerramienta guardar(AsignacionHerramienta asignacion);

	void eliminar(Long id);

	long contarAsignacionesActivas(Long herramientaId, Long excluirId);

	boolean tieneAsignacionActiva(Long herramientaId);

	List<AsignacionHerramienta> activasMasAntiguas(Long empleadoId, Long herramientaId, int cantidad);

	Map<Long, Long> asignacionesActivasPorHerramienta();

	void desvincularHerramienta(Long herramientaId);
}