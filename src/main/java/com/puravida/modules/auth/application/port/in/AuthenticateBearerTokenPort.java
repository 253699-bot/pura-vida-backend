package com.puravida.modules.auth.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;

public interface AuthenticateBearerTokenPort {

    AuthenticatedUser authenticate(String authorizationHeader);
}
