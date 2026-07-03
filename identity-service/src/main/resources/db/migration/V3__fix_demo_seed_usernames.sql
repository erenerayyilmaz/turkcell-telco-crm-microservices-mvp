-- V2 seed'i gercek Keycloak kullanicilariyla AYNI username/email'i kullaniyordu
-- (testuser/csruser). Gercek kullanicinin ilk POST /me/sync'i uniqueness kuralina
-- takilip 409 PROFILE_CONFLICT donuyordu (canli smoke testte dogrulandi).
-- Demo satirlari demo.* degerlerine tasinir; yalnizca demo keycloak_id'leri hedeflenir,
-- gercek kullanicilarin sync ile olusmus satirlarina dokunulmaz. Idempotent.
UPDATE user_profiles
SET username = 'demo.customer', email = 'demo.customer@example.com',
    first_name = 'Demo', last_name = 'Customer'
WHERE keycloak_id = 'demo-testuser' AND username = 'testuser';

UPDATE user_profiles
SET username = 'demo.csr', email = 'demo.csr@example.com',
    first_name = 'Demo', last_name = 'CSR'
WHERE keycloak_id = 'demo-csruser' AND username = 'csruser';
