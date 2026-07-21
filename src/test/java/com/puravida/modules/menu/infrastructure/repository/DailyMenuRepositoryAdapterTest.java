package com.puravida.modules.menu.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.menu.infrastructure.persistence.DailyMenuEntity;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.DishEntity;
import com.puravida.modules.menu.infrastructure.persistence.DishJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.MenuAvailabilityJpaRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyMenuRepositoryAdapterTest {

    @Mock
    private DailyMenuJpaRepository dailyMenuJpaRepository;

    @Mock
    private DishJpaRepository dishJpaRepository;

    @Mock
    private MenuAvailabilityJpaRepository availabilityJpaRepository;

    private DailyMenuRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DailyMenuRepositoryAdapter(
                dailyMenuJpaRepository,
                dishJpaRepository,
                availabilityJpaRepository
        );
    }

    @Test
    void omittedItemIsUnpublishedWithoutDeletingHistoricalRow() {
        LocalDate today = LocalDate.now();
        var entity = DailyMenuEntity.newItem(today, testDish(), 2);
        when(dailyMenuJpaRepository.findByFechaOrderByIdAsc(today)).thenReturn(List.of(entity));
        when(dailyMenuJpaRepository.findByFechaAndPublicadoTrueOrderByIdAsc(today)).thenReturn(List.of());

        var result = adapter.replaceForDate(today, List.of(), 2);

        assertThat(result).isEmpty();
        assertThat(entity.publicado()).isFalse();
        verify(dailyMenuJpaRepository).saveAll(List.of(entity));
    }

    @Test
    void previouslyOmittedItemCanBeRepublishedWithCurrentPrice() {
        LocalDate today = LocalDate.now();
        var dish = testDish();
        var entity = DailyMenuEntity.newItem(today, dish, 2);
        entity.unpublish();
        when(dailyMenuJpaRepository.findByFechaOrderByIdAsc(today)).thenReturn(List.of(entity));
        when(dailyMenuJpaRepository.findByFechaAndPublicadoTrueOrderByIdAsc(today))
                .thenReturn(List.of(entity));
        when(dishJpaRepository.findAllById(any())).thenReturn(List.of(DishEntity.fromDomain(dish)));
        when(availabilityJpaRepository.findByMenuIdIn(any())).thenReturn(List.of());

        var result = adapter.replaceForDate(today, List.of(dish), 2);

        assertThat(entity.publicado()).isTrue();
        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.publicado()).isTrue();
            assertThat(item.precioDia()).isEqualByComparingTo("65.00");
        });
    }

    @Test
    void itemThatRemainsPublishedKeepsItsDailyPriceSnapshot() {
        LocalDate today = LocalDate.now();
        var originalDish = testDish();
        var updatedCatalogDish = new com.puravida.modules.menu.domain.model.Dish(
                originalDish.id(),
                originalDish.nombre(),
                originalDish.descripcion(),
                originalDish.tipoPlatillo(),
                new java.math.BigDecimal("80.00"),
                originalDish.imagenKey(),
                true,
                originalDish.creadoEn(),
                java.time.LocalDateTime.now()
        );
        var entity = DailyMenuEntity.newItem(today, originalDish, 2);
        when(dailyMenuJpaRepository.findByFechaOrderByIdAsc(today)).thenReturn(List.of(entity));
        when(dailyMenuJpaRepository.findByFechaAndPublicadoTrueOrderByIdAsc(today))
                .thenReturn(List.of(entity));
        when(dishJpaRepository.findAllById(any()))
                .thenReturn(List.of(DishEntity.fromDomain(updatedCatalogDish)));
        when(availabilityJpaRepository.findByMenuIdIn(any())).thenReturn(List.of());

        var result = adapter.replaceForDate(today, List.of(updatedCatalogDish), 2);

        assertThat(result).singleElement().satisfies(item ->
                assertThat(item.precioDia()).isEqualByComparingTo("65.00")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"20.00", "80.00", "20.50", "79.99"})
    void updatesPublishedDishPriceForRequestedDate(String price) {
        LocalDate today = LocalDate.now();
        var currentDish = testDish();
        var updatedDish = new com.puravida.modules.menu.domain.model.Dish(
                currentDish.id(),
                currentDish.nombre(),
                currentDish.descripcion(),
                currentDish.tipoPlatillo(),
                new java.math.BigDecimal(price),
                currentDish.imagenKey(),
                true,
                currentDish.creadoEn(),
                java.time.LocalDateTime.now()
        );
        var entity = DailyMenuEntity.newItem(today, currentDish, 2);
        when(dailyMenuJpaRepository.findByFechaAndDishIdAndPublicadoTrueOrderByIdAsc(today, updatedDish.id()))
                .thenReturn(List.of(entity));

        adapter.updatePublishedDishForDate(today, updatedDish);

        assertThat(entity.precioDia()).isEqualByComparingTo(price);
        verify(dailyMenuJpaRepository).saveAll(List.of(entity));
    }

    private com.puravida.modules.menu.domain.model.Dish testDish() {
        return new com.puravida.modules.menu.domain.model.Dish(
                10,
                "Tacos",
                "Orden de tacos",
                "platillo_fuerte",
                new java.math.BigDecimal("65.00"),
                null,
                true,
                java.time.LocalDateTime.now(),
                null
        );
    }
}
