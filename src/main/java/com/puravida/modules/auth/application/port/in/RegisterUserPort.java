package com.puravida.modules.auth.application.port.in;

import com.puravida.modules.auth.application.dto.RegisterRequest;
import com.puravida.modules.auth.application.dto.UserSummaryResponse;

public interface RegisterUserPort {

    UserSummaryResponse register(RegisterRequest request);
}
