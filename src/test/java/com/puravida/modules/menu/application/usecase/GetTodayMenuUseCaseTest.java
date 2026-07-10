package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.domain.model.DailyMenuItem;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTodayMenuUseCaseTest {

    @Mock
    private DailyMenuRepositoryPort dailyMenuRepositoryPort;

    @InjectMocks
    private GetTodayMenuUseCase useCase;

    @Test
    void returnsNotConfiguredWhenTodayMenuDoesNotExist() {
        when(dailyMenuRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(List.of());

        var response = useCase.getToday();

        assertThat(response.configured()).isFalse();
        assertThat(response.fecha()).isNotNull();
        assertThat(response.items()).isEmpty();
    }

    @Test
    void returnsConfiguredMenuWhenItemsExist() {
        when(dailyMenuRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(List.of(TestMenuData.menuItem()));

        var response = useCase.getToday();

        assertThat(response.configured()).isTrue();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).platilloId()).isEqualTo(10);
    }
}
