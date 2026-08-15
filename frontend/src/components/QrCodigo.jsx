import { QRCodeSVG } from 'qrcode.react';

export default function QrCodigo({ codigo, tamano, mostrarTexto }) {
  if (!codigo) return <span className="sin-dato">&mdash;</span>;
  return (
    <div className="qr-celda">
      <QRCodeSVG value={codigo} size={tamano || 40} level="M" />
      {mostrarTexto !== false && <span className="qr-texto">{codigo}</span>}
    </div>
  );
}