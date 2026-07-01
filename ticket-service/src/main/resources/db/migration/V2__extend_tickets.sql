-- Durum makinesi icin ek zaman damgalari (additive; mevcut V1 semasi korunur).
ALTER TABLE tickets ADD COLUMN updated_at  TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE tickets ADD COLUMN resolved_at TIMESTAMP;

-- Sik kullanilan filtre/erisim yollari icin index'ler.
CREATE INDEX idx_tickets_status         ON tickets (status);
CREATE INDEX idx_tickets_customer_id    ON tickets (customer_id);
CREATE INDEX idx_tickets_assigned_to    ON tickets (assigned_to);
CREATE INDEX idx_ticket_comments_ticket ON ticket_comments (ticket_id);
