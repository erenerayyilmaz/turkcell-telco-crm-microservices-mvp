package com.turkcell.billingservice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** bill_cycles tablosunu karsilar (Flyway V1 + V3). Abonelik/ucret bilgisi bill-run'da fatura kesmek icindir. */
@Entity
@Table(name = "bill_cycles")
public class BillCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID customerId;
    private UUID subscriptionId;
    private BigDecimal monthlyFee;

    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency = "TRY";

    private Short dayOfMonth;
    private LocalDate nextRunDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Short getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Short dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public LocalDate getNextRunDate() { return nextRunDate; }
    public void setNextRunDate(LocalDate nextRunDate) { this.nextRunDate = nextRunDate; }
}
