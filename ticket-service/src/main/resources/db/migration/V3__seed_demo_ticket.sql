-- Demo destek talebi (customer-service'teki demo musteriye baglidir: 11111111-...).
INSERT INTO tickets (id, customer_id, category, priority, status, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'BILLING', 'HIGH', 'OPEN', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_comments (ticket_id, author_id, body, created_at)
VALUES ('22222222-2222-2222-2222-222222222222',
        '00000000-0000-0000-0000-000000000001',
        'Musteri son fatura tutarina itiraz etti; inceleme baslatildi.', now());
