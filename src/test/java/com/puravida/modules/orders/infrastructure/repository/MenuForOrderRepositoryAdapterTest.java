package com.puravida.modules.orders.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuEntity;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.DishEntity;
import com.puravida.modules.menu.infrastructure.persistence.DishJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.MenuAvailabilityJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuForOrderRepositoryAdapterTest {

    @Mock
    private DailyMenuJpaRepository dailyMenuJpaRepository;

    @Mock
    private DishJpaRepository dishJpaRepository;

    @Mock
    private MenuAvailabilityJpaRepository availabilityJpaRepository;

    private MenuForOrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MenuForOrderRepositoryAdapter(
                dailyMenuJpaRepository,
                dishJpaRepository,
                availabilityJpaRepository
        );
    }

    @Test
    void orderSourceReadsOnlyPublishedRows() {
        LocalDate today = LocalDate.now();
        Dish dish = dish();
        DailyMenuEntity published = DailyMenuEntity.newItem(today, dish, 2);
        when(dailyMenuJpaRepository.findByFechaAndPublicadoTrueOrderByIdAsc(today))
                .thenReturn(List.of(published));
        when(dishJpaRepository.findAllById(any())).thenReturn(List.of(DishEntity.fromDomain(dish)));
        when(availabilityJpaRepository.findByMenuIdIn(any())).thenReturn(List.of());

        var result = adapter.findByFecha(today);

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.dishId()).isEqualTo(10);
            assertThat(item.precioDia()).isEqualByComparingTo("65.00");
        });
        verify(dailyMenuJpaRepository).findByFechaAndPublicadoTrueOrderByIdAsc(today);
        verify(dailyMenuJpaRepository, never()).findByFechaOrderByIdAsc(today);
    }

    @Test
    void unpublishedRowsCannotReachOrderFlow() {
        LocalDate today = LocalDate.now();
        when(dailyMenuJpaRepository.findByFechaAndPublicadoTrueOrderByIdAsc(today))
                .thenReturn(List.of());

        assertThat(adapter.findByFecha(today)).isEmpty();
        verify(dailyMenuJpaRepository).findByFechaAndPublicadoTrueOrderByIdAsc(today);
    }

    private Dish dish() {
        return new Dish(
                10,
                "Tacos",
                "Orden de tacos",
                "platillo_fuerte",
                new BigDecimal("65.00"),
                null,
                true,
                LocalDateTime.now(),
                null
        );
    }
}
