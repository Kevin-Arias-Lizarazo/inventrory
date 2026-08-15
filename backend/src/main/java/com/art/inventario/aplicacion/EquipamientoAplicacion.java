package com.art.inventario.aplicacion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.art.inventario.dominio.AsignacionHerramienta;
import com.art.inventario.dominio.EntregaEpp;
import com.art.inventario.dominio.EntregaRopa;
import com.art.inventario.puerto.entrada.EquipamientoCasoDeUso;
import com.art.inventario.puerto.salida.AsignacionHerramientaPersistencia;
import com.art.inventario.puerto.salida.EmpleadoPersistencia;
import com.art.inventario.puerto.salida.EntregaEppPersistencia;
import com.art.inventario.puerto.salida.EntregaRopaPersistencia;

@Service
public class EquipamientoAplicacion implements EquipamientoCasoDeUso {

	private final EmpleadoPersistencia empleados;
	private final EntregaEppPersistencia entregasEpp;
	private final EntregaRopaPersistencia entregasRopa;
	private final AsignacionHerramientaPersistencia asignaciones;

	public EquipamientoAplicacion(EmpleadoPersistencia empleados, EntregaEppPersistencia entregasEpp,
			EntregaRopaPersistencia entregasRopa, AsignacionHerramientaPersistencia asignaciones) {
		this.empleados = empleados;
		this.entregasEpp = entregasEpp;
		this.entregasRopa = entregasRopa;
		this.asignaciones = asignaciones;
	}

	@Override
	public Map<String, Object> equipamientoEmpleado(Long empleadoId) {
		var emp = empleados.obtener(empleadoId);
		List<EntregaEpp> epp = entregasEpp.listar().stream()
				.filter(e -> e.getEmpleado() != null && empleadoId.equals(e.getEmpleado().getId())).toList();
		List<EntregaRopa> ropa = entregasRopa.listar().stream()
				.filter(e -> e.getEmpleado() != null && empleadoId.equals(e.getEmpleado().getId())).toList();
		List<AsignacionHerramienta> herr = asignaciones.listar().stream()
				.filter(a -> a.getEmpleado() != null && empleadoId.equals(a.getEmpleado().getId())).toList();
		Map<String, Object> out = new HashMap<>();
		out.put("empleado", emp);
		out.put("entregasEpp", epp);
		out.put("entregasRopa", ropa);
		out.put("asignacionesHerramientas", herr);
		return out;
	}
}
