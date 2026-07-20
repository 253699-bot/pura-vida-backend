package com.puravida.modules.business.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.dto.UpdateTodayBusinessStatusRequest;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTodayBusinessStatusUseCaseTest {

    @Mock
    private BusinessDayStatusRepositoryPort statusRepositoryPort;

    @Mock
    private ActiveBusinessOrdersPort activeBusinessOrdersPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UpdateTodayBusinessStatusUseCase useCase;

    @Test
    void closedStatusRequiresReason() {
        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(false, " "),
                authenticatedEncargada()
        )).isInstanceOf(BusinessStatusValidationException.class);

        verify(statusRepositoryPort, never()).save(any(BusinessDayStatus.class));
    }

    @Test
    void rejectsClientUsers() {
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(clientUser()));

        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(true, null),
                authenticatedEncargada()
        )).isInstanceOf(ForbiddenException.class);

        verify(statusRepositoryPort, never()).save(any(BusinessDayStatus.class));
    }

    @Test
    void createsStatusWhenTodayDoesNotExist() {
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(encargadaUser()));
        when(statusRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(Optional.empty());
        when(statusRepositoryPort.save(any(BusinessDayStatus.class))).thenAnswer(invocation -> {
            BusinessDayStatus status = invocation.getArgument(0);
            return new BusinessDayStatus(
                    12,
                    status.fecha(),
                    status.abierto(),
                    status.motivoCierre(),
                    status.registradoPor(),
                    status.creadoEn(),
                    status.actualizadoEn(),
                    status.cicloIniciadoEn()
            );
        });

        TodayBusinessStatusResponse response = useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(false, " Cierre por mantenimiento "),
                authenticatedEncargada()
        );

        ArgumentCaptor<BusinessDayStatus> statusCaptor = ArgumentCaptor.forClass(BusinessDayStatus.class);
        verify(statusRepositoryPort).save(statusCaptor.capture());
        BusinessDayStatus savedStatus = statusCaptor.getValue();

        assertThat(savedStatus.id()).isNull();
        assertThat(savedStatus.abierto()).isFalse();
        assertThat(savedStatus.motivoCierre()).isEqualTo("Cierre por mantenimiento");
        assertThat(savedStatus.registradoPor()).isEqualTo(2);
        assertThat(response.configured()).isTrue();
        assertThat(response.id()).isEqualTo(12);
    }

    @Test
    void updatesExistingStatusAndClearsClosingReasonWhenOpen() {
        LocalDate today = LocalDate.now();
        BusinessDayStatus currentStatus = new BusinessDayStatus(
                7,
                today,
                false,
                "Cierre anterior",
                2,
                LocalDateTime.now().minusDays(1),
                null
        );
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(encargadaUser()));
        when(statusRepositoryPort.findByFecha(today)).thenReturn(Optional.of(currentStatus));
        when(statusRepositoryPort.save(any(BusinessDayStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodayBusinessStatusResponse response = useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(true, "No debe persistir"),
                authenticatedEncargada()
        );

        assertThat(response.id()).isEqualTo(7);
        assertThat(response.abierto()).isTrue();
        assertThat(response.motivoCierre()).isNull();
        assertThat(response.actualizadoEn()).isNotNull();
    }

    @Test
    void blocksClosingWhenCurrentCycleHasPendingOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime cycleStartedAt = LocalDateTime.now().minusHours(4);
        BusinessDayStatus currentStatus = openStatus(today, cycleStartedAt);
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(encargadaUser()));
        when(statusRepositoryPort.findByFecha(today)).thenReturn(Optional.of(currentStatus));
        when(activeBusinessOrdersPort.countActiveSince(cycleStartedAt))
                .thenReturn(new ActiveBusinessOrderCounts(1, 0));

        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(false, "Fin de jornada"),
                authenticatedEncargada()
        ))
                .isInstanceOf(ActiveOrdersPreventClosureException.class)
                .hasMessage("No puedes cerrar la fonda mientras existan pedidos pendientes o aceptados.");

        verify(statusRepositoryPort, never()).save(any(BusinessDayStatus.class));
    }

    @Test
    void blocksClosingWhenCurrentCycleHasAcceptedOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime cycleStartedAt = LocalDateTime.now().minusHours(4);
        BusinessDayStatus currentStatus = openStatus(today, cycleStartedAt);
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(encargadaUser()));
        when(statusRepositoryPort.findByFecha(today)).thenReturn(Optional.of(currentStatus));
        when(activeBusinessOrdersPort.countActiveSince(cycleStartedAt))
                .thenReturn(new ActiveBusinessOrderCounts(0, 2));

        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(false, "Fin de jornada"),
                authenticatedEncargada()
        )).isInstanceOf(ActiveOrdersPreventClosureException.class);

        verify(statusRepositoryPort, never()).save(any(BusinessDayStatus.class));
    }

    @Test
    void allowsClosingWhenCurrentCycleHasNoPendingOrAcceptedOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime cycleStartedAt = LocalDateTime.now().minusHours(4);
        BusinessDayStatus currentStatus = openStatus(today, cycleStartedAt);
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(encargadaUser()));
        when(statusRepositoryPort.findByFecha(today)).thenReturn(Optional.of(currentStatus));
        when(activeBusinessOrdersPort.countActiveSince(cycleStartedAt))
                .thenReturn(new ActiveBusinessOrderCounts(0, 0));
        when(statusRepositoryPort.save(any(BusinessDayStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodayBusinessStatusResponse response = useCase.updateToday(
                new UpdateTodayBusinessStatusRequest(false, "Fin de jornada"),
                authenticatedEncargada()
        );

        assertThat(response.abierto()).isFalse();
        assertThat(response.motivoCierre()).isEqualTo("Fin de jornada");
    }

    private BusinessDayStatus openStatus(LocalDate today, LocalDateTime cycleStartedAt) {
        return new BusinessDayStatus(
                7,
                today,
                true,
                null,
                2,
                cycleStartedAt.minusMinutes(10),
                null,
                cycleStartedAt
        );
    }

    private AuthenticatedUser authenticatedEncargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }

    private User encargadaUser() {
        return user(UserRole.ENCARGADA, true);
    }

    private User clientUser() {
        return user(UserRole.CLIENTE, true);
    }

    private User user(UserRole role, boolean active) {
        return new User(
                2,
                "Usuario",
                "usuario@example.com",
                null,
                "hash",
                role,
                null,
                true,
                active,
                LocalDateTime.now().minusDays(1),
                null
        );
    }
}