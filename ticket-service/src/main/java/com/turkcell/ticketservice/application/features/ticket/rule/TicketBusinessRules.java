package com.turkcell.ticketservice.application.features.ticket.rule;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.turkcell.ticketservice.exception.InvalidTicketStateException;

/**
 * Destek talebi durum makinesi kurallari.
 * OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED; RESOLVED tekrar acilabilir (IN_PROGRESS).
 */
@Component
public class TicketBusinessRules {

    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "OPEN",        Set.of("IN_PROGRESS", "CLOSED"),
            "IN_PROGRESS", Set.of("RESOLVED", "CLOSED"),
            "RESOLVED",    Set.of("CLOSED", "IN_PROGRESS"),
            "CLOSED",      Set.of());

    /** targetStatus bilinen bir durum olmali ve mevcut durumdan gecise izin verilmeli. */
    public void assertTransitionAllowed(String from, String to) {
        if (!ALLOWED.containsKey(to)) {
            throw new InvalidTicketStateException("Bilinmeyen durum: " + to);
        }
        if (from.equals(to)) {
            throw new InvalidTicketStateException("Talep zaten '" + to + "' durumunda");
        }
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new InvalidTicketStateException("Gecersiz gecis: " + from + " -> " + to);
        }
    }
}
