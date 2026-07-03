package com.turkcell.identityservice.application.features.userprofile.rule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.turkcell.identityservice.exception.ProfileConflictException;
import com.turkcell.identityservice.repository.UserProfileRepository;

/** Benzersizlik kurallari: sahibi (ownerKeycloakId) haric username/email cakismasi -> 409. */
class UserProfileBusinessRulesTest {

    private UserProfileRepository repository;
    private UserProfileBusinessRules rules;

    @BeforeEach
    void setUp() {
        repository = mock(UserProfileRepository.class);
        rules = new UserProfileBusinessRules(repository);
    }

    @Test
    void usernameConflictThrows() {
        when(repository.existsByUsernameAndKeycloakIdNot("ali", "sub-1")).thenReturn(true);
        assertThatThrownBy(() -> rules.usernameMustBeUnique("ali", "sub-1"))
                .isInstanceOf(ProfileConflictException.class)
                .hasMessageContaining("ali");
    }

    @Test
    void usernameOwnedBySelfPasses() {
        when(repository.existsByUsernameAndKeycloakIdNot("ali", "sub-1")).thenReturn(false);
        assertThatCode(() -> rules.usernameMustBeUnique("ali", "sub-1")).doesNotThrowAnyException();
    }

    @Test
    void emailConflictThrows() {
        when(repository.existsByEmailAndKeycloakIdNot("a@x.com", "sub-1")).thenReturn(true);
        assertThatThrownBy(() -> rules.emailMustBeUnique("a@x.com", "sub-1"))
                .isInstanceOf(ProfileConflictException.class);
    }

    @Test
    void nullEmailSkipsCheck() {
        assertThatCode(() -> rules.emailMustBeUnique(null, "sub-1")).doesNotThrowAnyException();
        verifyNoInteractions(repository);
    }

    @Test
    void uniqueEmailPasses() {
        when(repository.existsByEmailAndKeycloakIdNot(anyString(), anyString())).thenReturn(false);
        assertThatCode(() -> rules.emailMustBeUnique("b@x.com", "sub-2")).doesNotThrowAnyException();
    }
}
