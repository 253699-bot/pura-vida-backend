package com.puravida.modules.auth.application.port.in;

import com.puravida.modules.auth.application.dto.AuthResponse;
import com.puravida.modules.auth.application.dto.LoginRequest;

public interface LoginUserPort {

    AuthResponse login(LoginRequest request);
}
