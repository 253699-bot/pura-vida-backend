package com.puravida.modules.users.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.dto.UserProfileResponse;
import com.puravida.modules.users.application.port.in.GetMyProfilePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyProfileUseCase implements GetMyProfilePort {

    private final UserProfileAuthorizationService authorizationService;

    public GetMyProfileUseCase(UserProfileAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse get(AuthenticatedUser authenticatedUser) {
        return UserProfileResponse.from(authorizationService.requireActiveUser(authenticatedUser));
    }
}
