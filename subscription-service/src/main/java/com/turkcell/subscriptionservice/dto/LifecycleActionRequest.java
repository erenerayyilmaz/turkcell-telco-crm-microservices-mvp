package com.turkcell.subscriptionservice.dto;

/** suspend/terminate istegi (opsiyonel govde): sebep audit detayina ve domain event'ine yazilir. */
public record LifecycleActionRequest(String reason) {
}
