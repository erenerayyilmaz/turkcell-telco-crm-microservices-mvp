import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "../../test/utils";

const { getMock } = vi.hoisted(() => ({ getMock: vi.fn() }));
vi.mock("../../lib/axios", () => ({ api: { get: getMock, post: vi.fn() } }));

import { BillingPage } from "./BillingPage";

const INVOICE = {
  id: "eeeeeeee-1111-2222-3333-444444444444",
  customerId: "cccccccc-1111-2222-3333-444444444444",
  subscriptionId: "dddddddd-1111-2222-3333-444444444444",
  billCycleId: null,
  periodStart: "2026-06-01",
  periodEnd: "2026-06-30",
  subTotal: 100,
  tax: 18,
  grandTotal: 118,
  status: "ISSUED",
  dueDate: "2026-07-15",
  issuedAt: "2026-07-01T09:00:00Z",
};
const LINE = {
  id: "l1",
  description: "Aylik tarife ucreti",
  quantity: 1,
  unitPrice: 100,
  lineTotal: 100,
};

function page(content: unknown[]) {
  return { data: { data: { content, number: 0, size: 10, totalElements: content.length } } };
}

describe("BillingPage", () => {
  beforeEach(() => {
    getMock.mockReset();
    getMock.mockImplementation((url: string) => {
      if (url === "/api/billing/invoices") return Promise.resolve(page([INVOICE]));
      if (url === "/api/customers") return Promise.resolve(page([]));
      if (url.endsWith("/pdf")) {
        return Promise.resolve({
          data: new Blob(["%PDF-1.4"], { type: "application/pdf" }),
          headers: { "content-disposition": 'attachment; filename="fatura-eeeeeeee.pdf"' },
        });
      }
      if (url.startsWith("/api/billing/invoices/")) {
        return Promise.resolve({ data: { data: { invoice: INVOICE, lines: [LINE] } } });
      }
      return Promise.resolve(page([]));
    });
    (URL as unknown as { createObjectURL: unknown }).createObjectURL = vi.fn(() => "blob:mock");
    (URL as unknown as { revokeObjectURL: unknown }).revokeObjectURL = vi.fn();
    vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("fatura listesini formatli tutar ve durum etiketiyle render eder", async () => {
    renderWithProviders(<BillingPage />);
    expect(await screen.findByText("118.00 TRY")).toBeInTheDocument();
    expect(screen.getByText("ISSUED")).toBeInTheDocument();
    expect(screen.getByText("2026-06-01 — 2026-06-30")).toBeInTheDocument();
  });

  it("'Detay' fatura + kalem detayini drawer'da acar", async () => {
    const user = userEvent.setup();
    renderWithProviders(<BillingPage />);
    await screen.findByText("118.00 TRY");

    await user.click(screen.getByRole("button", { name: /Detay/i }));
    expect(await screen.findByText("Aylik tarife ucreti")).toBeInTheDocument();
    await waitFor(() =>
      expect(getMock).toHaveBeenCalledWith(`/api/billing/invoices/${INVOICE.id}`),
    );
  });

  it("'PDF' aksiyonu blob olarak PDF ister ve indirir (G6)", async () => {
    const user = userEvent.setup();
    renderWithProviders(<BillingPage />);
    await screen.findByText("118.00 TRY");

    await user.click(screen.getByRole("button", { name: /PDF/i }));
    await waitFor(() =>
      expect(getMock).toHaveBeenCalledWith(
        `/api/billing/invoices/${INVOICE.id}/pdf`,
        expect.objectContaining({ responseType: "blob" }),
      ),
    );
    await waitFor(() =>
      expect(HTMLAnchorElement.prototype.click).toHaveBeenCalled(),
    );
  });
});
