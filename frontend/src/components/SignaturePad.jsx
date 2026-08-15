import { useCallback, useEffect, useRef } from 'react';

export default function SignaturePad({ valor, onCambio, alto }) {
  const lienzo = useRef(null);
  const dibujando = useRef(false);
  const valorRef = useRef(valor);

  function dibujar(url) {
    const canvas = lienzo.current;
    if (!canvas) return;
    const escala = window.devicePixelRatio || 1;
    const ctx = canvas.getContext('2d');
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.scale(escala, escala);
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, canvas.width / escala, canvas.height / escala);
    if (url) {
      const img = new Image();
      img.onload = () => ctx.drawImage(img, 0, 0, canvas.width / escala, canvas.height / escala);
      img.src = url;
    }
  }

  const tamano = useCallback(() => {
    const canvas = lienzo.current;
    if (!canvas) return;
    const anchoCompleto = canvas.parentElement.clientWidth - 2;
    const al = alto || 140;
    const escala = window.devicePixelRatio || 1;
    canvas.width = anchoCompleto * escala;
    canvas.height = al * escala;
    canvas.style.width = `${anchoCompleto}px`;
    canvas.style.height = `${al}px`;
    const ctx = canvas.getContext('2d');
    ctx.lineWidth = 2;
    ctx.strokeStyle = '#111827';
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    dibujar(valorRef.current);
  }, [alto]);

  useEffect(() => {
    valorRef.current = valor;
    dibujar(valor);
  }, [valor]);

  useEffect(() => {
    tamano();
    window.addEventListener('resize', tamano);
    return () => window.removeEventListener('resize', tamano);
  }, [tamano]);

  function punto(e) {
    const r = lienzo.current.getBoundingClientRect();
    return { x: e.clientX - r.left, y: e.clientY - r.top };
  }

  function apoyar(e) {
    const p = punto(e);
    dibujando.current = true;
    const ctx = lienzo.current.getContext('2d');
    ctx.beginPath();
    ctx.moveTo(p.x, p.y);
  }

  function mover(e) {
    if (!dibujando.current) return;
    const p = punto(e);
    const ctx = lienzo.current.getContext('2d');
    ctx.lineTo(p.x, p.y);
    ctx.stroke();
  }

  function soltar() {
    dibujando.current = false;
  }

  function exportar() {
    onCambio(lienzo.current.toDataURL('image/png'));
  }

  function limpiar() {
    onCambio(null);
    dibujar(null);
  }

  return (
    <div className="firma-caja">
      <canvas
        ref={lienzo}
        onPointerDown={apoyar}
        onPointerMove={mover}
        onPointerUp={soltar}
        onPointerLeave={soltar}
      />
      <div className="firma-botones">
        <button type="button" className="btn btn-borde" onClick={limpiar}>
          Limpiar
        </button>
        <button type="button" className="btn btn-primario" onClick={exportar}>
          Guardar firma
        </button>
      </div>
    </div>
  );
}