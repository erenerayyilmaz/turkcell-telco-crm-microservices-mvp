-- Abonelik yasam dongusu bildirimleri (G4, FR-14): suspend/reactivate/terminate SMS sablonlari.
INSERT INTO notification_templates (code, channel, locale, subject, body_template) VALUES
  ('SUBSCRIPTION_SUSPENDED', 'SMS', 'tr-TR', 'Hattiniz Askiya Alindi',
   'Hattiniz askiya alinmistir. Detayli bilgi icin musteri hizmetlerini arayabilirsiniz.'),
  ('SUBSCRIPTION_REACTIVATED', 'SMS', 'tr-TR', 'Hattiniz Yeniden Acildi',
   'Hattiniz yeniden kullanima acilmistir. Iyi gunlerde kullanin.'),
  ('SUBSCRIPTION_TERMINATED', 'SMS', 'tr-TR', 'Hattiniz Kapatildi',
   'Aboneliginiz sonlandirilmistir. Bizi tercih ettiginiz icin tesekkur ederiz.')
ON CONFLICT (code) DO NOTHING;
