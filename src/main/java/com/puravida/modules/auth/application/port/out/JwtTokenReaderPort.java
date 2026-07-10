package com.puravida.modules.auth.application.port.out;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;

public interface JwtTokenReaderPort {

    AuthenticatedUser read(String token);
}
