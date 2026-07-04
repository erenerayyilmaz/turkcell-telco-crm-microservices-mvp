import { Button, Descriptions, Drawer, Result, Space, Spin, Table, Tag, Typography } from "antd";
import { useQuery } from "@tanstack/react-query";
import { api } from "../../lib/axios";
import { ShortId } from "../../components/ShortId";
import type { ApiResponse, InvoiceDetailResponse, InvoiceLineResponse } from "../../api/types";

/** invoices durumlari (Invoice.java): DRAFT -> ISSUED -> PAID | PAYMENT_FAILED. */
export const INVOICE_STATUS_COLOR: Record<string, string> = {
  DRAFT: "default",
  ISSUED: "blue",
  PAID: "green",
  PAYMENT_FAILED: "red",
};

/** InvoiceResponse'ta currency alani yok; sistem geneli TRY (PDF de bill-cycle yoksa TRY'ye duser). */
export function formatMoney(value: number | null | undefined): string {
  return `${(value ?? 0).toFixed(2)} TRY`;
}

interface Props {
  invoiceId: string | null;
  onClose: () => void;
}

/**
 * Tekil fatura gorunumu: GET /api/billing/invoices/{id} (fatura + kalemleri).
 * Salt-okunur; PDF indirme liste sayfasindaki aksiyonda (G6).
 */
export function InvoiceDetailDrawer({ invoiceId, onClose }: Props) {
  const { data, isFetching, isError, refetch } = useQuery({
    queryKey: ["invoice", invoiceId],
    queryFn: async () => {
      const res = await api.get<ApiResponse<InvoiceDetailResponse>>(
        `/api/billing/invoices/${invoiceId}`,
      );
      return res.data.data!;
    },
    enabled: !!invoiceId,
  });

  const invoice = data?.invoice;

  return (
    <Drawer title="Fatura detayi" width={560} open={!!invoiceId} onClose={onClose} destroyOnHidden>
      {isError && (
        <Result
          status="warning"
          title="Fatura alinamadi"
          extra={
            <Button type="primary" onClick={() => void refetch()}>
              Yeniden dene
            </Button>
          }
        />
      )}
      {!isError && (isFetching || !invoice) && (
        <Spin style={{ display: "block", margin: "80px auto" }} size="large" />
      )}
      {!isError && !isFetching && invoice && (
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <Descriptions column={1} size="small" bordered>
            <Descriptions.Item label="Fatura">
              <ShortId value={invoice.id} />
            </Descriptions.Item>
            <Descriptions.Item label="Musteri">
              <ShortId value={invoice.customerId} />
            </Descriptions.Item>
            <Descriptions.Item label="Abonelik">
              <ShortId value={invoice.subscriptionId} />
            </Descriptions.Item>
            <Descriptions.Item label="Donem">
              {invoice.periodStart ?? "-"} — {invoice.periodEnd ?? "-"}
            </Descriptions.Item>
            <Descriptions.Item label="Durum">
              <Tag color={INVOICE_STATUS_COLOR[invoice.status] ?? "default"}>{invoice.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Son odeme">{invoice.dueDate ?? "-"}</Descriptions.Item>
          </Descriptions>

          <Table<InvoiceLineResponse>
            rowKey="id"
            size="small"
            pagination={false}
            dataSource={data?.lines}
            columns={[
              { title: "Aciklama", dataIndex: "description" },
              { title: "Adet", dataIndex: "quantity", align: "right" },
              {
                title: "Birim fiyat",
                dataIndex: "unitPrice",
                align: "right",
                render: (v: number) => formatMoney(v),
              },
              {
                title: "Tutar",
                dataIndex: "lineTotal",
                align: "right",
                render: (v: number) => formatMoney(v),
              },
            ]}
          />

          <Descriptions column={1} size="small" bordered>
            <Descriptions.Item label="Ara toplam">{formatMoney(invoice.subTotal)}</Descriptions.Item>
            <Descriptions.Item label="KDV">{formatMoney(invoice.tax)}</Descriptions.Item>
            <Descriptions.Item label="Genel toplam">
              <Typography.Text strong>{formatMoney(invoice.grandTotal)}</Typography.Text>
            </Descriptions.Item>
          </Descriptions>
        </Space>
      )}
    </Drawer>
  );
}
