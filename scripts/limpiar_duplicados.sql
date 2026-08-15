-- Mantenimiento: eliminar duplicados por nombre manteniendo el id menor,
-- re-apuntando las referencias de las filas que se eliminan.
BEGIN;

-- ============ EMPLEADOS ============
UPDATE minutas SET empleado_id = (
  SELECT MIN(id) FROM empleados k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM empleados WHERE id = minutas.empleado_id))
) WHERE empleado_id IS NOT NULL;

UPDATE contratos SET empleado_id = (
  SELECT MIN(id) FROM empleados k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM empleados WHERE id = contratos.empleado_id))
) WHERE empleado_id IS NOT NULL;

UPDATE entregas_ropa SET empleado_id = (
  SELECT MIN(id) FROM empleados k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM empleados WHERE id = entregas_ropa.empleado_id))
) WHERE empleado_id IS NOT NULL;

UPDATE entregas_epp SET empleado_id = (
  SELECT MIN(id) FROM empleados k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM empleados WHERE id = entregas_epp.empleado_id))
) WHERE empleado_id IS NOT NULL;

UPDATE asignaciones_herramientas SET empleado_id = (
  SELECT MIN(id) FROM empleados k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM empleados WHERE id = asignaciones_herramientas.empleado_id))
) WHERE empleado_id IS NOT NULL;

UPDATE asignaciones_consumibles SET empleado_id = (
  SELECT MIN(id) FROM empleados k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM empleados WHERE id = asignaciones_consumibles.empleado_id))
) WHERE empleado_id IS NOT NULL;

DELETE FROM empleados WHERE id NOT IN (SELECT MIN(id) FROM empleados GROUP BY lower(nombre));

-- ============ MATERIALES ============
UPDATE movimientos_materiales SET material_id = (
  SELECT MIN(id) FROM materiales k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM materiales WHERE id = movimientos_materiales.material_id))
) WHERE material_id IS NOT NULL;

DELETE FROM materiales WHERE id NOT IN (SELECT MIN(id) FROM materiales GROUP BY lower(nombre));

-- ============ CONSUMIBLES ============
UPDATE movimientos_consumibles SET consumible_id = (
  SELECT MIN(id) FROM consumibles k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM consumibles WHERE id = movimientos_consumibles.consumible_id))
) WHERE consumible_id IS NOT NULL;

UPDATE asignaciones_consumibles SET consumible_id = (
  SELECT MIN(id) FROM consumibles k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM consumibles WHERE id = asignaciones_consumibles.consumible_id))
) WHERE consumible_id IS NOT NULL;

DELETE FROM consumibles WHERE id NOT IN (SELECT MIN(id) FROM consumibles GROUP BY lower(nombre));

-- ============ HERRAMIENTAS ============
UPDATE asignaciones_herramientas SET herramienta_id = (
  SELECT MIN(id) FROM herramientas k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM herramientas WHERE id = asignaciones_herramientas.herramienta_id))
) WHERE herramienta_id IS NOT NULL;

DELETE FROM herramientas WHERE id NOT IN (SELECT MIN(id) FROM herramientas GROUP BY lower(nombre));

-- ============ PROYECTOS ============
UPDATE minutas SET proyecto_id = (
  SELECT MIN(id) FROM proyectos k
  WHERE lower(k.nombre) = lower((SELECT nombre FROM proyectos WHERE id = minutas.proyecto_id))
) WHERE proyecto_id IS NOT NULL;

DELETE FROM proyectos WHERE id NOT IN (SELECT MIN(id) FROM proyectos GROUP BY lower(nombre));

-- ============ ÍNDICES ÚNICOS POR NOMBRE ============
CREATE UNIQUE INDEX IF NOT EXISTS uq_empleados_nombre ON empleados(lower(nombre));
CREATE UNIQUE INDEX IF NOT EXISTS uq_materiales_nombre ON materiales(lower(nombre));
CREATE UNIQUE INDEX IF NOT EXISTS uq_consumibles_nombre ON consumibles(lower(nombre));
CREATE UNIQUE INDEX IF NOT EXISTS uq_herramientas_nombre ON herramientas(lower(nombre));
CREATE UNIQUE INDEX IF NOT EXISTS uq_proyectos_nombre ON proyectos(lower(nombre));

COMMIT;
