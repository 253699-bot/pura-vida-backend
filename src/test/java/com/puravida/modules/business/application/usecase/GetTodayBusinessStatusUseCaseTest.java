package com.puravida.modules.business.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.port.out.BusinessDayStatusRepositoryPort;
import com.puravida.modules.business.domain.model.BusinessDayStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTodayBusinessStatusUseCaseTest {

    @Mock
    private BusinessDayStatusRepositoryPort repositoryPort;

    @InjectMocks
    private GetTodayBusinessStatusUseCase useCase;

    @Test
    void returnsNotConfiguredWhenTodayStatusDoesNotExist() {
        when(repositoryPort.findByFecha(any(LocalDate.class))).thenReturn(Optional.empty());

        TodayBusinessStatusResponse response = useCase.getToday();

        assertThat(response.configured()).isFalse();
        assertThat(response.fecha()).isNotNull();
        assertThat(response.abierto()).isNull();
        verify(repositoryPort, never()).save(any(BusinessDayStatus.class));
    }

    @Test
    void returnsConfiguredStatusWhenTodayStatusExists() {
        LocalDate today = LocalDate.now();
        BusinessDayStatus status = new BusinessDayStatus(
                7,
                today,
                true,
                null,
                2,
                LocalDateTime.now().minusHours(2),
                null
        );
        when(repositoryPort.findByFecha(today)).thenReturn(Optional.of(status));

        TodayBusinessStatusResponse response = useCase.getToday();

        assertThat(response.configured()).isTrue();
        assertThat(response.id()).isEqualTo(7);
        assertThat(response.abierto()).isTrue();
        assertThat(response.registradoPor()).isEqualTo(2);
    }
}
