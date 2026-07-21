package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.UpdateDishPort;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDishUseCase implements UpdateDishPort {

    private final DishRepositoryPort dishRepositoryPort;
    private final DailyMenuRepositoryPort dailyMenuRepositoryPort;
    private final MenuAuthorizationService authorizationService;

    public UpdateDishUseCase(
            DishRepositoryPort dishRepositoryPort,
            DailyMenuRepositoryPort dailyMenuRepositoryPort,
            MenuAuthorizationService authorizationService
    ) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.dailyMenuRepositoryPort = dailyMenuRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public DishResponse update(
            Integer dishId,
            CreateDishRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        authorizationService.requireEncargada(authenticatedUser);
        Dish dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        if (!dish.activo()) {
            throw new ConflictException("No se puede editar un platillo retirado.");
        }

        Dish updated = dish.updateDetails(
                request.nombre().trim(),
                normalizeNullable(request.descripcion()),
                request.tipoPlatillo().trim(),
                request.precioBase()
        );
        Dish savedDish = dishRepositoryPort.save(updated);
        dailyMenuRepositoryPort.updatePublishedDishForDate(LocalDate.now(), savedDish);
        return DishResponse.from(savedDish);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
