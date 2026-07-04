import { describe, it, expect } from "vitest";
import { filenameFromDisposition } from "./download";

describe("filenameFromDisposition", () => {
  it("Content-Disposition'dan dosya adini cikarir", () => {
    expect(filenameFromDisposition('attachment; filename="fatura-abcd1234.pdf"', "fb.pdf")).toBe(
      "fatura-abcd1234.pdf",
    );
  });

  it("tirnaksiz filename'i de cozer", () => {
    expect(filenameFromDisposition("attachment; filename=rapor.pdf", "fb.pdf")).toBe("rapor.pdf");
  });

  it("header yoksa fallback doner", () => {
    expect(filenameFromDisposition(undefined, "fb.pdf")).toBe("fb.pdf");
  });
});
