import { useState } from "react";
import { App, Button, Card, Select, Space, Table, Tag } from "antd";
import { EyeOutlined, FilePdfOutlined } from "@ant-design/icons";
import { keepPreviousData, useMutation, useQuery } from "@tanstack/react-query";
import { api } from "../../lib/axios";
import { apiErrorMessage } from "../../lib/apiError";
import { filenameFromDisposition, triggerBlobDownload } from "../../lib/download";
import { ShortId } from "../../components/ShortId";
import { CustomerSelect } from "../../components/CustomerSelect";
import { formatMoney, InvoiceDetailDrawer, INVOICE_STATUS_COLOR } from "./InvoiceDetailDrawer";
import type { ApiResponse, InvoiceResponse, RestPage } from "../../api/types";

const INVOICE_STATUSES = ["DRAFT", "ISSUED", "PAID", "PAYMENT_FAILED"];

/**
 * Faturalar (BILLING_ADMIN/CSR/ADMIN): server-side sayfali liste + customerId/status
 * filtreleri; satirdan kalem detayi (drawer) ve PDF indirme (G6, GET .../{id}/pdf).
 */
export function BillingPage() {
  const { message } = App.useApp();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [status, setStatus] = useState<string | undefined>();
  const [customerId, setCustomerId] = useState<string | undefined>();
  const [detailId, setDetailId] = useState<string | null>(null);

  const { data, isFetching } = useQuery({
    queryKey: ["invoices", page, pageSize, status, customerId],
    queryFn: async () => {
      const res = await api.get<ApiResponse<RestPage<InvoiceResponse>>>("/api/billing/invoices", {
        params: {
          page,
          size: pageSize,
          ...(status ? { status } : {}),
          ...(customerId ? { customerId } : {}),
        },
      });
      return res.data.data!;
    },
    placeholderData: keepPreviousData,
  });

  const downloadPdf = useMutation({
    mutationFn: async (id: string) => {
      const res = await api.get<Blob>(`/api/billing/invoices/${id}/pdf`, { responseType: "blob" });
      const filename = filenameFromDisposition(
        res.headers?.["content-disposition"] as string | undefined,
        `fatura-${id.slice(0, 8)}.pdf`,
      );
      triggerBlobDownload(res.data, filename);
    },
    onError: (error) => message.error(apiErrorMessage(error, "PDF indirilemedi")),
  });

  return (
    <Card
      title="Faturalar"
      extra={
        <Space wrap>
          <CustomerSelect
            value={customerId}
            onChange={(v) => {
              setPage(0);
              setCustomerId(v);
            }}
            placeholder="Musteri filtrele"
            style={{ width: 260 }}
            allowClear
          />
          <Select
            placeholder="Durum filtrele"
            allowClear
            style={{ width: 200 }}
            value={status}
            onChange={(v) => {
              setPage(0);
              setStatus(v);
            }}
            options={INVOICE_STATUSES.map((s) => ({ value: s, label: s }))}
          />
        </Space>
      }
    >
      <Table<InvoiceResponse>
        rowKey="id"
        loading={isFetching}
        dataSource={data?.content}
        pagination={{
          current: page + 1,
          pageSize,
          total: data?.totalElements ?? 0,
          showSizeChanger: true,
          onChange: (p, s) => {
            setPage(p - 1);
            setPageSize(s);
          },
        }}
        columns={[
          { title: "Fatura", dataIndex: "id", render: (v: string) => <ShortId value={v} /> },
          {
            title: "Musteri",
            dataIndex: "customerId",
            render: (v: string) => <ShortId value={v} />,
          },
          {
            title: "Donem",
            key: "period",
            render: (_, r) => `${r.periodStart ?? "-"} — ${r.periodEnd ?? "-"}`,
          },
          {
            title: "Genel toplam",
            dataIndex: "grandTotal",
            align: "right",
            render: (v: number) => formatMoney(v),
          },
          {
            title: "Durum",
            dataIndex: "status",
            render: (s: string) => <Tag color={INVOICE_STATUS_COLOR[s] ?? "default"}>{s}</Tag>,
          },
          { title: "Son odeme", dataIndex: "dueDate", render: (v: string | null) => v ?? "-" },
          {
            title: "",
            key: "actions",
            render: (_, record) => (
              <Space>
                <Button
                  size="small"
                  icon={<EyeOutlined />}
                  onClick={() => setDetailId(record.id)}
                >
                  Detay
                </Button>
                <Button
                  size="small"
                  icon={<FilePdfOutlined />}
                  loading={downloadPdf.isPending && downloadPdf.variables === record.id}
                  onClick={() => downloadPdf.mutate(record.id)}
                >
                  PDF
                </Button>
              </Space>
            ),
          },
        ]}
      />

      <InvoiceDetailDrawer invoiceId={detailId} onClose={() => setDetailId(null)} />
    </Card>
  );
}
