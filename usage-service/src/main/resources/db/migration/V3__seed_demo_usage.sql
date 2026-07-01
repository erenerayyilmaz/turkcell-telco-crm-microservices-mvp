-- Demo kullanim kayitlari (agregasyon/list sorgularini gostermek icin).
-- Sabit demo abonelik: 33333333-... (canli saga'nin urettigi abonelige bagli DEGILDIR).
INSERT INTO usage_records (subscription_id, type, quantity, recorded_at, cdr_ref) VALUES
  ('33333333-3333-3333-3333-333333333333', 'VOICE', 12.5000, now() - interval '2 days', 'CDR-V-0001'),
  ('33333333-3333-3333-3333-333333333333', 'VOICE',  3.0000, now() - interval '1 days', 'CDR-V-0002'),
  ('33333333-3333-3333-3333-333333333333', 'SMS',    4.0000, now() - interval '1 days', 'CDR-S-0001'),
  ('33333333-3333-3333-3333-333333333333', 'DATA', 512.0000, now(),                     'CDR-D-0001');
