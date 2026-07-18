package com.puravida.modules.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher passwordHasher = new BCryptPasswordHasher();

    @Test
    void hashesAndVerifiesPasswordWithBCrypt() {
        String hash = passwordHasher.hash("password123");

        assertThat(hash).isNotEqualTo("password123").startsWith("$2");
        assertThat(passwordHasher.matches("password123", hash)).isTrue();
        assertThat(passwordHasher.matches("wrong-password", hash)).isFalse();
    }
}
