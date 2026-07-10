package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.TodayMenuResponse;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuItemRequest;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuRequest;
import com.puravida.modules.menu.application.port.in.UpdateTodayMenuPort;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.exception.MenuValidationException;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTodayMenuUseCase implements UpdateTodayMenuPort {

    private final DailyMenuRepositoryPort dailyMenuRepositoryPort;
    private final DishRepositoryPort dishRepositoryPort;
    private final MenuAuthorizationService authorizationService;

    public UpdateTodayMenuUseCase(
            DailyMenuRepositoryPort dailyMenuRepositoryPort,
            DishRepositoryPort dishRepositoryPort,
            MenuAuthorizationService authorizationService
    ) {
        this.dailyMenuRepositoryPort = dailyMenuRepositoryPort;
        this.dishRepositoryPort = dishRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public TodayMenuResponse updateToday(UpdateTodayMenuRequest request, AuthenticatedUser authenticatedUser) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        List<Integer> dishIds = validateAndExtractDishIds(request);
        Map<Integer, Dish> dishesById = dishRepositoryPort.findAllByIds(dishIds).stream()
                .collect(Collectors.toMap(Dish::id, Function.identity()));

        List<Dish> dishes = dishIds.stream()
                .map(id -> findValidDish(id, dishesById))
                .toList();

        LocalDate today = LocalDate.now();
        return TodayMenuResponse.configured(
                today,
                dailyMenuRepositoryPort.replaceForDate(today, dishes, actor.id())
        );
    }

    private List<Integer> validateAndExtractDishIds(UpdateTodayMenuRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new MenuValidationException("El menu debe incluir al menos un platillo.");
        }

        HashSet<Integer> seen = new HashSet<>();
        return request.items().stream()
                .map(UpdateTodayMenuItemRequest::platilloId)
                .peek(this::validateDishId)
                .peek(id -> {
                    if (!seen.add(id)) {
                        throw new MenuValidationException("El menu no puede incluir platillos duplicados.");
                    }
                })
                .toList();
    }

    private void validateDishId(Integer dishId) {
        if (dishId == null || dishId <= 0) {
            throw new MenuValidationException("Todos los platillos del menu deben ser validos.");
        }
    }

    private Dish findValidDish(Integer dishId, Map<Integer, Dish> dishesById) {
        Dish dish = dishesById.get(dishId);
        if (dish == null) {
            throw new NotFoundException("No se encontro el platillo solicitado.");
        }

        if (!dish.activo()) {
            throw new MenuValidationException("Solo se pueden publicar platillos activos.");
        }

        return dish;
    }
}
