import { useState } from "react";
import { App, Button, Card, Form, Input, InputNumber, Modal, Select, Table, Tag } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/axios";
import { apiErrorMessage } from "../../lib/apiError";
import { useAuth } from "../../auth/useAuth";
import type { ApiResponse, RestPage, TariffResponse } from "../../api/types";

const TYPE_COLOR: Record<string, string> = {
  POSTPAID: "blue",
  PREPAID: "purple",
};

interface TariffFormValues {
  code: string;
  name: string;
  type: string;
  monthlyFee: number;
  minutesIncluded?: number;
  smsIncluded?: number;
  dataMbIncluded?: number;
}

/**
 * Tarife katalogu: okuma her kimlik dogrulanmis kullaniciya acik,
 * tarife olusturma yalnizca CATALOG_ADMIN (buton rolle gizlenir, gercek yetki backend'de).
 */
export function TariffsPage() {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();
  const canCreate = hasRole("CATALOG_ADMIN");

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<TariffFormValues>();

  const { data, isFetching } = useQuery({
    queryKey: ["tariffs", page, pageSize],
    queryFn: async () => {
      const res = await api.get<ApiResponse<RestPage<TariffResponse>>>("/api/catalog/tariffs", {
        params: { page, size: pageSize },
      });
      return res.data.data!;
    },
    placeholderData: keepPreviousData,
  });

  const createTariff = useMutation({
    mutationFn: (values: TariffFormValues) =>
      api.post<ApiResponse<TariffResponse>>("/api/catalog/tariffs", values),
    onSuccess: (res) => {
      message.success(res.data.message ?? "Tarife olusturuldu");
      setModalOpen(false);
      // Liste + siparis formundaki tarife select'i ("tariffs" prefix'i) birlikte tazelenir.
      void queryClient.invalidateQueries({ queryKey: ["tariffs"] });
    },
    onError: (error) => message.error(apiErrorMessage(error, "Tarife olusturulamadi")),
  });

  const openCreate = () => {
    form.resetFields();
    setModalOpen(true);
  };

  return (
    <Card
      title="Tarifeler"
      extra={
        canCreate && (
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Yeni tarife
          </Button>
        )
      }
    >
      <Table<TariffResponse>
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
          { title: "Kod", dataIndex: "code" },
          { title: "Ad", dataIndex: "name" },
          {
            title: "Tip",
            dataIndex: "type",
            render: (t: string) => <Tag color={TYPE_COLOR[t] ?? "default"}>{t}</Tag>,
          },
          {
            title: "Aylik ucret",
            dataIndex: "monthlyFee",
            align: "right",
            render: (v: number) => `${v} TRY`,
          },
          { title: "Dakika", dataIndex: "minutesIncluded", align: "right" },
          { title: "SMS", dataIndex: "smsIncluded", align: "right" },
          {
            title: "Internet",
            dataIndex: "dataMbIncluded",
            align: "right",
            render: (v: number | null) => (v == null ? "-" : `${v} MB`),
          },
          {
            title: "Durum",
            dataIndex: "status",
            render: (s: string) => <Tag color={s === "ACTIVE" ? "green" : "default"}>{s}</Tag>,
          },
        ]}
      />

      <Modal
        title="Yeni tarife"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="Olustur"
        confirmLoading={createTariff.isPending}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={(values) => createTariff.mutate(values)}>
          <Form.Item
            name="code"
            label="Kod"
            rules={[{ required: true, whitespace: true, message: "Kod girin" }]}
            extra="Ornek: TARIFE_XL — _FAIL ile biten kodlar demo'da aktivasyon hatasi tetikler."
          >
            <Input placeholder="TARIFE_XL" />
          </Form.Item>
          <Form.Item
            name="name"
            label="Ad"
            rules={[{ required: true, whitespace: true, message: "Ad girin" }]}
          >
            <Input placeholder="Telco Mega" />
          </Form.Item>
          <Form.Item name="type" label="Tip" rules={[{ required: true, message: "Tip secin" }]}>
            <Select
              placeholder="Tip sec"
              options={[
                { value: "POSTPAID", label: "POSTPAID" },
                { value: "PREPAID", label: "PREPAID" },
              ]}
            />
          </Form.Item>
          <Form.Item
            name="monthlyFee"
            label="Aylik ucret (TRY)"
            rules={[{ required: true, message: "Ucret girin" }]}
            extra="1000 TRY ustu tarifeler demo'da odeme reddine dusur."
          >
            <InputNumber min={0.01} step={10} style={{ width: "100%" }} placeholder="249.90" />
          </Form.Item>
          <Form.Item name="minutesIncluded" label="Dahil dakika">
            <InputNumber min={0} style={{ width: "100%" }} placeholder="1500" />
          </Form.Item>
          <Form.Item name="smsIncluded" label="Dahil SMS">
            <InputNumber min={0} style={{ width: "100%" }} placeholder="1000" />
          </Form.Item>
          <Form.Item name="dataMbIncluded" label="Dahil internet (MB)">
            <InputNumber min={0} style={{ width: "100%" }} placeholder="15360" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
