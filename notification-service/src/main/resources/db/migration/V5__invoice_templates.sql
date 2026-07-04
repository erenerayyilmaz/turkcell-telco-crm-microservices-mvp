-- Fatura yasam dongusu bildirimleri (G2, docx senaryo 14.2 / §8.8): EMAIL (mock) sablonlari.
INSERT INTO notification_templates (code, channel, locale, subject, body_template) VALUES
  ('INVOICE_GENERATED', 'EMAIL', 'tr-TR', 'Faturaniz Kesildi',
   'Donem faturaniz olusturuldu. Tutar ve son odeme tarihini Hesabim''dan goruntuleyebilirsiniz.'),
  ('INVOICE_PAID', 'EMAIL', 'tr-TR', 'Odemeniz Alindi',
   'Fatura odemeniz basariyla alinmistir. Tesekkur ederiz.'),
  ('INVOICE_PAYMENT_FAILED', 'EMAIL', 'tr-TR', 'Odeme Basarisiz',
   'Fatura odemeniz alinamadi. Lutfen odeme yonteminizi kontrol edin; tahsilat yeniden denenecektir.')
ON CONFLICT (code) DO NOTHING;
