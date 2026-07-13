package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.CreateDishPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDishUseCase implements CreateDishPort {

    private final DishRepositoryPort dishRepositoryPort;
    private final MenuAuthorizationService authorizationService;

    public CreateDishUseCase(DishRepositoryPort dishRepositoryPort, MenuAuthorizationService authorizationService) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public DishResponse create(CreateDishRequest request, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        Dish savedDish = dishRepositoryPort.save(Dish.create(
                request.nombre().trim(),
                normalizeNullable(request.descripcion()),
                request.tipoPlatillo().trim(),
                request.precioBase()
        ));
        return DishResponse.from(savedDish);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
