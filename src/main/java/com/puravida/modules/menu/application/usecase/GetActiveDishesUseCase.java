package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.GetActiveDishesPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetActiveDishesUseCase implements GetActiveDishesPort {

    private final DishRepositoryPort dishRepositoryPort;
    private final MenuAuthorizationService authorizationService;

    public GetActiveDishesUseCase(DishRepositoryPort dishRepositoryPort, MenuAuthorizationService authorizationService) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> getActive(AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        return dishRepositoryPort.findAllActive().stream().map(DishResponse::from).toList();
    }
}
