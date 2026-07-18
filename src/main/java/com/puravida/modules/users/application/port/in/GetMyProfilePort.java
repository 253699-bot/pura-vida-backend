package com.puravida.modules.users.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.dto.UserProfileResponse;

public interface GetMyProfilePort {

    UserProfileResponse get(AuthenticatedUser authenticatedUser);
}
