package com.art.inventario.puerto.entrada;
import java.util.Map;
public interface ImportacionCasoDeUso {
	Map<String, Object> importarCsv(String recurso, String csv);
}
