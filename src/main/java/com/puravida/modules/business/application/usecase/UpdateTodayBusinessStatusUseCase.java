package com.puravida.modules.business.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.dto.UpdateTodayBusinessStatusRequest;
import com.puravida.modules.business.application.port.in.UpdateTodayBusinessStatusPort;
import com.puravida.modules.business.application.port.out.ActiveBusinessOrdersPort;
import com.puravida.modules.business.application.port.out.BusinessDayStatusRepositoryPort;
import com.puravida.modules.business.domain.exception.ActiveOrdersPreventClosureException;
import com.puravida.modules.business.domain.exception.BusinessStatusValidationException;
import com.puravida.modules.business.domain.model.ActiveBusinessOrderCounts;
import com.puravida.modules.business.domain.model.BusinessDayStatus;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTodayBusinessStatusUseCase implements UpdateTodayBusinessStatusPort {

    private final BusinessDayStatusRepositoryPort statusRepositoryPort;
    private final ActiveBusinessOrdersPort activeBusinessOrdersPort;
    private final UserRepositoryPort userRepositoryPort;

    public UpdateTodayBusinessStatusUseCase(
            BusinessDayStatusRepositoryPort statusRepositoryPort,
            ActiveBusinessOrdersPort activeBusinessOrdersPort,
            UserRepositoryPort userRepositoryPort
    ) {
        this.statusRepositoryPort = statusRepositoryPort;
        this.activeBusinessOrdersPort = activeBusinessOrdersPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional
    public TodayBusinessStatusResponse updateToday(
            UpdateTodayBusinessStatusRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        boolean abierto = validateOpenFlag(request);
        String motivoCierre = normalizeMotivoCierre(abierto, request.motivoCierre());
        User actor = resolveEncargada(authenticatedUser);
        LocalDate today = LocalDate.now();
        java.util.Optional<BusinessDayStatus> currentStatus = statusRepositoryPort.findByFecha(today);

        if (!abierto) {
            ensureNoActiveOrdersInCurrentCycle(currentStatus, today);
        }

        BusinessDayStatus status = currentStatus
                .map(current -> current.updateStatus(abierto, motivoCierre, actor.id()))
                .orElseGet(() -> BusinessDayStatus.create(today, abierto, motivoCierre, actor.id()));

        return TodayBusinessStatusResponse.from(statusRepositoryPort.save(status));
    }

    private void ensureNoActiveOrdersInCurrentCycle(
            java.util.Optional<BusinessDayStatus> currentStatus,
            LocalDate today
    ) {
        currentStatus
                .filter(BusinessDayStatus::abierto)
                .map(status -> cycleStartOrFallback(status, today))
                .map(activeBusinessOrdersPort::countActiveSince)
                .filter(ActiveBusinessOrderCounts::hasActiveOrders)
                .ifPresent(counts -> {
                    throw new ActiveOrdersPreventClosureException(counts);
                });
    }

    private LocalDateTime cycleStartOrFallback(BusinessDayStatus status, LocalDate today) {
        if (status.cicloIniciadoEn() != null) {
            return status.cicloIniciadoEn();
        }

        if (status.creadoEn() != null) {
            return status.creadoEn();
        }

        return today.atStartOfDay();
    }

    private boolean validateOpenFlag(UpdateTodayBusinessStatusRequest request) {
        if (request.abierto() == null) {
            throw new BusinessStatusValidationException("El estado abierto es obligatorio.");
        }
        return request.abierto();
    }

    private String normalizeMotivoCierre(boolean abierto, String motivoCierre) {
        if (abierto) {
            return null;
        }

        if (motivoCierre == null || motivoCierre.isBlank()) {
            throw new BusinessStatusValidationException(
                    "El motivo de cierre es obligatorio cuando la fonda esta cerrada."
            );
        }

        return motivoCierre.trim();
    }

    private User resolveEncargada(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }

        User actor = userRepositoryPort.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ForbiddenException("No tienes permisos para actualizar el estado de la fonda."));

        if (!actor.activo() || actor.rol() != UserRole.ENCARGADA) {
            throw new ForbiddenException("No tienes permisos para actualizar el estado de la fonda.");
        }

        return actor;
    }
}