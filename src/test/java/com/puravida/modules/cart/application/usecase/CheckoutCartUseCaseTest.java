package com.puravida.modules.cart.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.orders.application.dto.CreateOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.CreateOrderPort;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.ForbiddenException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutCartUseCaseTest {

    @Mock
    private CartRepositoryPort cartRepositoryPort;

    @Mock
    private MenuForOrderRepositoryPort menuRepositoryPort;

    @Mock
    private CreateOrderPort createOrderPort;

    @Mock
    private CartAuthorizationService authorizationService;

    private CheckoutCartUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckoutCartUseCase(
                cartRepositoryPort,
                menuRepositoryPort,
                createOrderPort,
                authorizationService
        );
    }

    @Test
    void createsOrderFromAuthenticatedUsersCartAndThenClearsIt() {
        AuthenticatedUser authenticatedUser = authenticatedClient();
        when(authorizationService.requireClient(authenticatedUser)).thenReturn(client());
        when(cartRepositoryPort.findByUserIdForUpdate(1)).thenReturn(List.of(cartItem()));
        when(menuRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(List.of(availableMenuItem()));
        when(createOrderPort.create(any(CreateOrderRequest.class), eq(authenticatedUser))).thenReturn(orderResponse());

        OrderResponse response = useCase.checkout(authenticatedUser);

        assertThat(response.id()).isEqualTo(10);
        assertThat(response.total()).isEqualByComparingTo("180.00");

        ArgumentCaptor<CreateOrderRequest> requestCaptor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(createOrderPort).create(requestCaptor.capture(), eq(authenticatedUser));
        assertThat(requestCaptor.getValue().items()).singleElement().satisfies(item -> {
            assertThat(item.menuItemId()).isEqualTo(20);
            assertThat(item.cantidad()).isEqualTo(2);
        });
        assertThat(requestCaptor.getValue().notas()).isNull();
        verify(cartRepositoryPort).findByUserIdForUpdate(1);
        verify(cartRepositoryPort).deleteByUserId(1);
    }

    @Test
    void rejectsEmptyCartWithoutCreatingOrder() {
        when(authorizationService.requireClient(authenticatedClient())).thenReturn(client());
        when(cartRepositoryPort.findByUserIdForUpdate(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.checkout(authenticatedClient()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El carrito esta vacio.");

        verifyNoInteractions(menuRepositoryPort, createOrderPort);
        verify(cartRepositoryPort, never()).deleteByUserId(any());
    }

    @Test
    void rejectsDishThatIsNoLongerAvailable() {
        when(authorizationService.requireClient(authenticatedClient())).thenReturn(client());
        when(cartRepositoryPort.findByUserIdForUpdate(1)).thenReturn(List.of(cartItem()));
        when(menuRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(List.of(unavailableMenuItem()));

        assertThatThrownBy(() -> useCase.checkout(authenticatedClient()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Uno de los platillos del carrito no esta disponible.");

        verifyNoInteractions(createOrderPort);
        verify(cartRepositoryPort, never()).deleteByUserId(any());
    }

    @Test
    void rejectsDishThatIsNoLongerActive() {
        when(authorizationService.requireClient(authenticatedClient())).thenReturn(client());
        when(cartRepositoryPort.findByUserIdForUpdate(1)).thenReturn(List.of(cartItem()));
        when(menuRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(List.of(inactiveMenuItem()));

        assertThatThrownBy(() -> useCase.checkout(authenticatedClient()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Uno de los platillos del carrito no esta disponible.");

        verifyNoInteractions(createOrderPort);
        verify(cartRepositoryPort, never()).deleteByUserId(any());
    }

    @Test
    void keepsCartWhenOrderCreationFails() {
        when(authorizationService.requireClient(authenticatedClient())).thenReturn(client());
        when(cartRepositoryPort.findByUserIdForUpdate(1)).thenReturn(List.of(cartItem()));
        when(menuRepositoryPort.findByFecha(any(LocalDate.class))).thenReturn(List.of(availableMenuItem()));
        when(createOrderPort.create(any(CreateOrderRequest.class), eq(authenticatedClient())))
                .thenThrow(new ConflictException("La fonda esta cerrada y no puede recibir pedidos."));

        assertThatThrownBy(() -> useCase.checkout(authenticatedClient()))
                .isInstanceOf(ConflictException.class);

        verify(cartRepositoryPort, never()).deleteByUserId(any());
    }

    @Test
    void rejectsNonClientBeforeReadingCart() {
        when(authorizationService.requireClient(authenticatedClient()))
                .thenThrow(new ForbiddenException("Solo los clientes pueden operar el carrito."));

        assertThatThrownBy(() -> useCase.checkout(authenticatedClient()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Solo los clientes pueden operar el carrito.");

        verifyNoInteractions(cartRepositoryPort, menuRepositoryPort, createOrderPort);
    }

    private AuthenticatedUser authenticatedClient() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    private User client() {
        return new User(1, "Cliente", "cliente@example.com", null, "hash", UserRole.CLIENTE,
                null, true, true, LocalDateTime.now(), null);
    }

    private CartItem cartItem() {
        return new CartItem(8, 1, 2, 2, new BigDecimal("85.00"), LocalDateTime.now(), null);
    }

    private OrderableMenuItem availableMenuItem() {
        return new OrderableMenuItem(
                20, LocalDate.now(), 2, "Comida corrida", new BigDecimal("90.00"), true, true
        );
    }

    private OrderableMenuItem unavailableMenuItem() {
        return new OrderableMenuItem(
                20, LocalDate.now(), 2, "Comida corrida", new BigDecimal("90.00"), false, true
        );
    }

    private OrderableMenuItem inactiveMenuItem() {
        return new OrderableMenuItem(
                20, LocalDate.now(), 2, "Comida corrida", new BigDecimal("90.00"), true, false
        );
    }

    private OrderResponse orderResponse() {
        return new OrderResponse(
                10,
                1,
                "Cliente",
                "pendiente",
                LocalDate.now(),
                LocalTime.NOON,
                new BigDecimal("180.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
    }
}
