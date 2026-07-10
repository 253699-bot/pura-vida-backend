package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.TodayMenuItemResponse;
import com.puravida.modules.menu.application.dto.UpdateMenuItemAvailabilityRequest;
import com.puravida.modules.menu.application.port.in.UpdateTodayMenuItemAvailabilityPort;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.domain.exception.MenuValidationException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTodayMenuItemAvailabilityUseCase implements UpdateTodayMenuItemAvailabilityPort {

    private final DailyMenuRepositoryPort dailyMenuRepositoryPort;
    private final MenuAuthorizationService authorizationService;

    public UpdateTodayMenuItemAvailabilityUseCase(
            DailyMenuRepositoryPort dailyMenuRepositoryPort,
            MenuAuthorizationService authorizationService
    ) {
        this.dailyMenuRepositoryPort = dailyMenuRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public TodayMenuItemResponse updateAvailability(
            Integer menuItemId,
            UpdateMenuItemAvailabilityRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        authorizationService.requireEncargada(authenticatedUser);

        if (request.disponible() == null) {
            throw new MenuValidationException("La disponibilidad es obligatoria.");
        }

        var currentItem = dailyMenuRepositoryPort.findItemById(menuItemId)
                .orElseThrow(() -> new NotFoundException("No se encontro el item del menu."));

        if (!LocalDate.now().equals(currentItem.fecha())) {
            throw new NotFoundException("No se encontro el item en el menu de hoy.");
        }

        return TodayMenuItemResponse.from(
                dailyMenuRepositoryPort.updateAvailability(menuItemId, request.disponible())
        );
    }
}
