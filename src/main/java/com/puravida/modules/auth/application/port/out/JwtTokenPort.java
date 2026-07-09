package com.puravida.modules.auth.application.port.out;

import com.puravida.modules.users.domain.model.User;

public interface JwtTokenPort {

    String generate(User user);

    long expirationMinutes();
}
