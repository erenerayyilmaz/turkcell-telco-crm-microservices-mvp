/**
 * Binary indirme yardimcilari (G6 fatura PDF gibi attachment yanitlar icin).
 * axios responseType:"blob" ile gelen icerigi tarayicida indirir; dosya adini
 * Content-Disposition header'indan cozer, yoksa fallback kullanir.
 */

/** `attachment; filename="fatura-x.pdf"` -> `fatura-x.pdf`; cozulemezse fallback. */
export function filenameFromDisposition(
  disposition: string | undefined,
  fallback: string,
): string {
  const match = disposition?.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
  return match?.[1] ? decodeURIComponent(match[1]) : fallback;
}

/** Blob'u gecici bir <a download> ile indirir ve object URL'i serbest birakir. */
export function triggerBlobDownload(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}
