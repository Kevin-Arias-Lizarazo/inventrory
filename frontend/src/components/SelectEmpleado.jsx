export default function SelectEmpleado({ empleados, valor, onCambio, requerido, soloContratados }) {
  return (
    <select
      value={valor || ''}
      onChange={(e) => onCambio(e.target.value ? Number(e.target.value) : null)}
      required={requerido}
    >
      <option value="">&mdash; Seleccione empleado &mdash;</option>
      {empleados.map((e) => {
        const contratado = e.contratado;
        const deshabilitado = soloContratados && contratado === false;
        return (
          <option key={e.id} value={e.id} disabled={deshabilitado}>
            {e.nombre}
            {contratado === false ? ' — no contratado' : ''}
          </option>
        );
      })}
    </select>
  );
}