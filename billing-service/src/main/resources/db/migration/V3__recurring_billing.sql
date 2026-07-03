-- Recurring billing (Faz 2): aylik bill-run + otomatik tahsilat.
-- bill_cycles, fatura kesebilmek icin abonelik + ucret bilgisini tasir
-- (OrderConfirmed event'inden doldurulur; eski satirlarda NULL kalabilir -> bill-run atlar).
ALTER TABLE bill_cycles ADD COLUMN subscription_id UUID;
ALTER TABLE bill_cycles ADD COLUMN monthly_fee     DECIMAL(10,2);
ALTER TABLE bill_cycles ADD COLUMN currency        CHAR(3) NOT NULL DEFAULT 'TRY';

-- Bill-run taramasi icin: vadesi gelen donguleri hizli bul.
CREATE INDEX idx_bill_cycles_next_run ON bill_cycles (next_run_date);

-- Transactional outbox: ChargeInvoiceCommand payment-commands topic'ine buradan publish edilir
-- (payment V2'deki tabloyla ayni DDL - platform deseni).
CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(50),
    aggregate_id   UUID NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    destination    VARCHAR(100) NOT NULL,
    payload        TEXT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count    INTEGER NOT NULL DEFAULT 0,
    error_message  TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    processed_at   TIMESTAMP,
    published_at   TIMESTAMP
);
CREATE INDEX idx_billing_outbox_pending ON outbox_events (created_at) WHERE status = 'PENDING';
