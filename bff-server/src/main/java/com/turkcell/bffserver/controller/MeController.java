package com.turkcell.bffserver.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * FE'nin "kim giris yapti, hangi roller var" ihtiyaci (FRONTEND.md §6/§9: menu + route guard).
 * Roller ID token'da DEGIL, session'daki ACCESS token'in realm_access.roles claim'indedir;
 * access token BFF'e Keycloak'tan dogrudan geldigi icin imza dogrulamadan payload okunur.
 *
 * NOT: Bu controller, /api/** gateway route'undan ONCE eslesir (annotated controller
 * mapping'i RouterFunction gateway mapping'inden onceliklidir) — /api/me gateway'e gitmez.
 */
@RestController
public class MeController {

    /** FE'nin bekledigi kullanici gorunumu. */
    public record MeResponse(String username, String email, String fullName, List<String> roles) {
    }

    private final ObjectMapper objectMapper;

    public MeController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/me")
    public MeResponse me(@AuthenticationPrincipal OidcUser user,
                         @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient client) {
        return new MeResponse(
                user.getPreferredUsername(),
                user.getEmail(),
                user.getFullName(),
                extractRealmRoles(client.getAccessToken().getTokenValue()));
    }

    /** Access token (JWT) payload'indan realm_access.roles dizisini okur. */
    private List<String> extractRealmRoles(String accessToken) {
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            return List.of();
        }
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        JsonNode roles = objectMapper.readTree(new String(payload, StandardCharsets.UTF_8))
                .path("realm_access").path("roles");
        if (!roles.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(roles.spliterator(), false)
                .map(JsonNode::asString)
                .toList();
    }
}
