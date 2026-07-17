package com.puravida.modules.cart.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.cart.infrastructure.persistence.CartItemJpaEntity;
import com.puravida.modules.cart.infrastructure.persistence.CartItemJpaRepository;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Lock;

@ExtendWith(MockitoExtension.class)
class CartRepositoryAdapterTest {

    @Mock
    private CartItemJpaRepository cartItemJpaRepository;

    private CartRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CartRepositoryAdapter(cartItemJpaRepository);
    }

    @Test
    void loadsAuthenticatedUsersCartWithWriteLock() {
        CartItem item = cartItem();
        when(cartItemJpaRepository.findByUserIdForUpdate(1))
                .thenReturn(List.of(CartItemJpaEntity.fromDomain(item)));

        List<CartItem> result = adapter.findByUserIdForUpdate(1);

        assertThat(result).containsExactly(item);
        verify(cartItemJpaRepository).findByUserIdForUpdate(1);
    }

    @Test
    void checkoutQueryUsesPessimisticWriteLock() throws NoSuchMethodException {
        Lock lock = CartItemJpaRepository.class
                .getMethod("findByUserIdForUpdate", Integer.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void physicallyDeletesAllItemsForUser() {
        when(cartItemJpaRepository.deleteByUserId(1)).thenReturn(2L);

        adapter.deleteByUserId(1);

        verify(cartItemJpaRepository).deleteByUserId(1);
    }

    private CartItem cartItem() {
        return new CartItem(8, 1, 2, 2, new BigDecimal("85.00"), LocalDateTime.now(), null);
    }
}
