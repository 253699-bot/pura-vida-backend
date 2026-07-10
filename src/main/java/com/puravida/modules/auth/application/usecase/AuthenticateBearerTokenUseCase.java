package com.puravida.modules.auth.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.auth.application.port.out.JwtTokenReaderPort;
import com.puravida.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateBearerTokenUseCase implements AuthenticateBearerTokenPort {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenReaderPort jwtTokenReaderPort;

    public AuthenticateBearerTokenUseCase(JwtTokenReaderPort jwtTokenReaderPort) {
        this.jwtTokenReaderPort = jwtTokenReaderPort;
    }

    @Override
    public AuthenticatedUser authenticate(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }

        if (!authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new UnauthorizedException("Token de autenticacion invalido.");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new UnauthorizedException("Token de autenticacion invalido.");
        }

        return jwtTokenReaderPort.read(token);
    }
}
