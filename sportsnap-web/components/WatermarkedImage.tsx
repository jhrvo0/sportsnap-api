"use client";

import { useEffect, useRef, useState } from "react";

type Props = {
  src: string;
  alt?: string;
  className?: string;
  watermark?: string;
};

export function WatermarkedImage({ src, alt = "", className = "", watermark = "PREVIEW • SportSnap" }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [dataUrl, setDataUrl] = useState<string>("");

  useEffect(() => {
    if (!src) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const img = new Image();
    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;

      // Desenha a imagem original
      ctx.drawImage(img, 0, 0);

      // Escurece levemente para destacar o texto
      ctx.fillStyle = "rgba(0,0,0,0.25)";
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // Configura fonte e opacidade do watermark
      const fontSize = Math.max(18, Math.round(canvas.width / 10));
      ctx.font = `bold ${fontSize}px Inter, Arial, sans-serif`;
      ctx.fillStyle = "rgba(255,255,255,0.55)";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";

      // Repete o texto em grade diagonal
      ctx.save();
      ctx.translate(canvas.width / 2, canvas.height / 2);
      ctx.rotate(-Math.PI / 5);

      const stepX = canvas.width * 0.7;
      const stepY = fontSize * 3.5;
      const cols = 4;
      const rows = 6;

      for (let r = -rows; r <= rows; r++) {
        for (let c = -cols; c <= cols; c++) {
          ctx.fillText(watermark, c * stepX, r * stepY);
        }
      }
      ctx.restore();

      setDataUrl(canvas.toDataURL("image/jpeg", 0.92));
    };
    img.src = src;
  }, [src, watermark]);

  return (
    <>
      <canvas ref={canvasRef} className="hidden" />
      {dataUrl ? (
        <img src={dataUrl} alt={alt} className={className} draggable={false} />
      ) : (
        <div className={`${className} bg-ink-200 animate-pulse`} />
      )}
    </>
  );
}
