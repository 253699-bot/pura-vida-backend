package com.puravida.modules.auth.application.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMinutes,
        UserSummaryResponse user
) {

    public static AuthResponse bearer(String token, long expiresInMinutes, UserSummaryResponse user) {
        return new AuthResponse(token, "Bearer", expiresInMinutes, user);
    }
}
