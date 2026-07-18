package com.puravida.modules.users.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.dto.UpdateMyProfileRequest;
import com.puravida.modules.users.application.dto.UserProfileResponse;

public interface UpdateMyProfilePort {

    UserProfileResponse update(UpdateMyProfileRequest request, AuthenticatedUser authenticatedUser);
}
