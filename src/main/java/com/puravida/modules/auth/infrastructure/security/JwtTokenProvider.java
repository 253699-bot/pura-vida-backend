package com.puravida.modules.auth.infrastructure.security;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.out.JwtTokenPort;
import com.puravida.modules.auth.application.port.out.JwtTokenReaderPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements JwtTokenPort, JwtTokenReaderPort {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generate(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim("userId", user.id())
                .claim("correo", user.correo())
                .claim("rol", user.rol().databaseValue())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public long expirationMinutes() {
        return expirationMinutes;
    }

    @Override
    public AuthenticatedUser read(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new AuthenticatedUser(
                    integerClaim(claims.get("userId")),
                    claims.get("correo", String.class),
                    UserRole.fromDatabaseValue(claims.get("rol", String.class))
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Token de autenticacion invalido.");
        }
    }

    private Integer integerClaim(Object value) {
        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        if (value instanceof String stringValue) {
            return Integer.valueOf(stringValue);
        }

        throw new IllegalArgumentException("Claim numerico invalido.");
    }
}
