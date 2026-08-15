const BASE = process.argv[2] || "http://localhost:8080";

async function main() {
  const recibidos = [];
  const res = await fetch(`${BASE}/api/cambios/suscripcion`);
  if (!res.ok) {
    console.log(`FALLÓ la suscripción: HTTP ${res.status}`);
    process.exit(1);
  }
  const reader = res.body.getReader();
  const dec = new TextDecoder();
  let buffer = "";

  const bomba = async () => {
    const { done, value } = await reader.read();
    if (done) return;
    buffer += dec.decode(value, { stream: true });
    let idx;
    while ((idx = buffer.indexOf("\n\n")) !== -1) {
      const bloque = buffer.slice(0, idx);
      buffer = buffer.slice(idx + 2);
      for (const linea of bloque.split("\n")) {
        if (linea.startsWith("data:")) {
          const d = linea.slice(5).trim();
          try {
            recibidos.push(JSON.parse(d));
          } catch {
            /* ignorar */
          }
        }
      }
    }
    return bomba();
  };
  bomba();

  await new Promise((r) => setTimeout(r, 800));

  const m = await (
    await fetch(`${BASE}/api/materiales`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nombre: "Prueba SSE " + Date.now(), unidad: "unidad" }),
    })
  ).json();

  await fetch(`${BASE}/api/materiales/${m.id}/movimientos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ tipo: "INGRESO", cantidad: 5, fecha: "2026-08-12", observacion: "test sse" }),
  });

  await fetch(`${BASE}/api/materiales/${m.id}`, { method: "DELETE" });

  await new Promise((r) => setTimeout(r, 1500));

  const recursos = [...new Set(recibidos.map((r) => r.recurso))];
  console.log("eventos recibidos:", JSON.stringify(recibidos));
  const ok = recursos.includes("materiales") && recursos.includes("movimientos-materiales");
  console.log(ok ? "SSE OK" : "SSE FAIL");
  process.exit(ok ? 0 : 1);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});