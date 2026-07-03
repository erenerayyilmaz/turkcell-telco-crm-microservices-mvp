package com.turkcell.ticketservice.application.features.ticket.rule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.turkcell.ticketservice.exception.InvalidTicketStateException;

/** Durum makinesi matrisi: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED (+ reopen). */
class TicketBusinessRulesTest {

    private final TicketBusinessRules rules = new TicketBusinessRules();

    @ParameterizedTest(name = "{0} -> {1} GECERLI")
    @CsvSource({
            "OPEN,        IN_PROGRESS",
            "OPEN,        CLOSED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CLOSED",
            "RESOLVED,    CLOSED",
            "RESOLVED,    IN_PROGRESS", // reopen
    })
    void validTransitions(String from, String to) {
        assertThatCode(() -> rules.assertTransitionAllowed(from, to)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} -> {1} GECERSIZ")
    @CsvSource({
            "OPEN,        RESOLVED",   // adim atlama
            "CLOSED,      IN_PROGRESS", // terminal
            "CLOSED,      RESOLVED",
            "CLOSED,      OPEN",
            "RESOLVED,    OPEN",
            "IN_PROGRESS, OPEN",
    })
    void invalidTransitions(String from, String to) {
        assertThatThrownBy(() -> rules.assertTransitionAllowed(from, to))
                .isInstanceOf(InvalidTicketStateException.class);
    }

    @Test
    void unknownTargetRejected() {
        assertThatThrownBy(() -> rules.assertTransitionAllowed("OPEN", "ARCHIVED"))
                .isInstanceOf(InvalidTicketStateException.class)
                .hasMessageContaining("Bilinmeyen");
    }

    @Test
    void sameStateRejected() {
        assertThatThrownBy(() -> rules.assertTransitionAllowed("OPEN", "OPEN"))
                .isInstanceOf(InvalidTicketStateException.class)
                .hasMessageContaining("zaten");
    }
}
