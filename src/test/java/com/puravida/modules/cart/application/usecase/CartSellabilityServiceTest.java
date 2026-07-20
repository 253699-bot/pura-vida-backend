package com.puravida.modules.cart.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.port.out.BusinessStatusForOrderRepositoryPort;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartSellabilityServiceTest {

    @Mock
    private MenuForOrderRepositoryPort menuRepositoryPort;

    @Mock
    private BusinessStatusForOrderRepositoryPort businessStatusRepositoryPort;

    @InjectMocks
    private CartSellabilityService service;

    @Test
    void returnsPublishedAvailableDishWhenBusinessIsOpen() {
        when(businessStatusRepositoryPort.findOpenByFecha(any())).thenReturn(Optional.of(true));
        when(menuRepositoryPort.findByFecha(any())).thenReturn(List.of(menuItem(true, true)));

        var item = service.requireSellableDish(2);

        assertThat(item.id()).isEqualTo(20);
        assertThat(item.precioDia()).isEqualByComparingTo("90.00");
    }

    @Test
    void rejectsCartChangesWhileBusinessIsClosed() {
        when(businessStatusRepositoryPort.findOpenByFecha(any())).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> service.requireSellableDish(2))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cerrada");
    }

    @Test
    void rejectsDishMissingFromPublishedMenu() {
        when(businessStatusRepositoryPort.findOpenByFecha(any())).thenReturn(Optional.of(true));
        when(menuRepositoryPort.findByFecha(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.requireSellableDish(2))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("menu publicado");
    }

    @Test
    void rejectsUnavailableOrInactiveDish() {
        when(businessStatusRepositoryPort.findOpenByFecha(any())).thenReturn(Optional.of(true));
        when(menuRepositoryPort.findByFecha(any())).thenReturn(List.of(menuItem(false, true)));

        assertThatThrownBy(() -> service.requireSellableDish(2))
                .isInstanceOf(ConflictException.class);

        when(menuRepositoryPort.findByFecha(any())).thenReturn(List.of(menuItem(true, false)));
        assertThatThrownBy(() -> service.requireSellableDish(2))
                .isInstanceOf(ConflictException.class);
    }

    private OrderableMenuItem menuItem(boolean disponible, boolean activo) {
        return new OrderableMenuItem(
                20,
                LocalDate.now(),
                2,
                "Comida corrida",
                new BigDecimal("90.00"),
                disponible,
                activo
        );
    }
}
