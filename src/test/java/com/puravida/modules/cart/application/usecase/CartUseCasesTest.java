package com.puravida.modules.cart.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.AddCartItemRequest;
import com.puravida.modules.cart.application.dto.UpdateCartItemQuantityRequest;
import com.puravida.modules.cart.application.port.out.CartDishRepositoryPort;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.model.CartDish;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartUseCasesTest {

    @Mock
    private CartRepositoryPort cartRepositoryPort;

    @Mock
    private CartDishRepositoryPort cartDishRepositoryPort;

    @Mock
    private CartAuthorizationService authorizationService;

    private AddCartItemUseCase addCartItemUseCase;
    private UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    private DeleteCartItemUseCase deleteCartItemUseCase;
    private GetCartUseCase getCartUseCase;

    private final CartResponseAssembler responseAssembler = new CartResponseAssembler();

    @BeforeEach
    void setUp() {
        addCartItemUseCase = new AddCartItemUseCase(
                cartRepositoryPort, cartDishRepositoryPort, authorizationService, responseAssembler
        );
        updateCartItemQuantityUseCase = new UpdateCartItemQuantityUseCase(
                cartRepositoryPort, cartDishRepositoryPort, authorizationService, responseAssembler
        );
        deleteCartItemUseCase = new DeleteCartItemUseCase(cartRepositoryPort, authorizationService);
        getCartUseCase = new GetCartUseCase(
                cartRepositoryPort, cartDishRepositoryPort, authorizationService, responseAssembler
        );
    }

    @Test
    void addsActiveDishWithPriceSnapshot() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(cartDishRepositoryPort.findById(2)).thenReturn(Optional.of(activeDish()));
        when(cartRepositoryPort.findByUserIdAndDishId(1, 2)).thenReturn(Optional.empty());
        when(cartRepositoryPort.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            return new CartItem(8, item.userId(), item.dishId(), item.cantidad(), item.precioUnitario(), item.creadoEn(), null);
        });

        var response = addCartItemUseCase.add(new AddCartItemRequest(2, 2), authenticatedUser());

        assertThat(response.id()).isEqualTo(8);
        assertThat(response.precioUnitario()).isEqualByComparingTo("85.00");
        assertThat(response.subtotal()).isEqualByComparingTo("170.00");
    }

    @Test
    void rejectsMissingOrInactiveDish() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(cartDishRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addCartItemUseCase.add(new AddCartItemRequest(99, 1), authenticatedUser()))
                .isInstanceOf(NotFoundException.class);

        when(cartDishRepositoryPort.findById(3)).thenReturn(Optional.of(new CartDish(3, "Inactivo", BigDecimal.TEN, false)));
        assertThatThrownBy(() -> addCartItemUseCase.add(new AddCartItemRequest(3, 1), authenticatedUser()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updatesOnlyQuantityForOwnedItem() {
        CartItem item = cartItem();
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(cartRepositoryPort.findById(8)).thenReturn(Optional.of(item));
        when(cartDishRepositoryPort.findById(2)).thenReturn(Optional.of(activeDish()));
        when(cartRepositoryPort.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = updateCartItemQuantityUseCase.updateQuantity(8, new UpdateCartItemQuantityRequest(3), authenticatedUser());

        assertThat(response.cantidad()).isEqualTo(3);
        assertThat(response.precioUnitario()).isEqualByComparingTo("85.00");
    }

    @Test
    void physicallyDeletesOnlyOwnedItem() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(cartRepositoryPort.findById(8)).thenReturn(Optional.of(cartItem()));

        deleteCartItemUseCase.delete(8, authenticatedUser());

        verify(cartRepositoryPort).deleteById(8);
    }

    @Test
    void rejectsMissingOrForeignCartItem() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(cartRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteCartItemUseCase.delete(99, authenticatedUser()))
                .isInstanceOf(NotFoundException.class);

        CartItem foreignItem = new CartItem(7, 2, 2, 1, BigDecimal.TEN, LocalDateTime.now(), null);
        when(cartRepositoryPort.findById(7)).thenReturn(Optional.of(foreignItem));
        assertThatThrownBy(() -> deleteCartItemUseCase.delete(7, authenticatedUser()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void returnsOnlyTheAuthenticatedUsersCart() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(cartRepositoryPort.findByUserId(1)).thenReturn(List.of(cartItem()));
        when(cartDishRepositoryPort.findAllByIds(List.of(2))).thenReturn(List.of(activeDish()));

        var response = getCartUseCase.getCart(authenticatedUser());

        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo("170.00");
        verify(cartRepositoryPort).findByUserId(1);
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    private User user() {
        return new User(1, "Cliente", "cliente@example.com", null, "hash", UserRole.CLIENTE,
                null, true, true, LocalDateTime.now(), null);
    }

    private CartDish activeDish() {
        return new CartDish(2, "Comida corrida", new BigDecimal("85.00"), true);
    }

    private CartItem cartItem() {
        return new CartItem(8, 1, 2, 2, new BigDecimal("85.00"), LocalDateTime.now(), null);
    }
}
