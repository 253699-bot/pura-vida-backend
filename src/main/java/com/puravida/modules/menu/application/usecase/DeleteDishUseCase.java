package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.DeleteDishPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteDishUseCase implements DeleteDishPort {

    private final DishRepositoryPort dishRepositoryPort;
    private final MenuAuthorizationService authorizationService;

    public DeleteDishUseCase(DishRepositoryPort dishRepositoryPort, MenuAuthorizationService authorizationService) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public DishResponse delete(Integer dishId, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        Dish dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        if (!dish.activo()) {
            throw new ConflictException("El platillo ya esta eliminado.");
        }

        return DishResponse.from(dishRepositoryPort.save(dish.deactivate()));
    }
}
