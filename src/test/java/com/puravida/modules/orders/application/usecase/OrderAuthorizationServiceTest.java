package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.shared.domain.exception.ForbiddenException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderAuthorizationServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private OrderAuthorizationService authorizationService;

    @Test
    void allowsActiveClientToReadOrderHistory() {
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(TestOrderData.client()));

        assertThat(authorizationService.requireClienteForHistory(TestOrderData.authenticatedClient()))
                .isEqualTo(TestOrderData.client());
    }

    @Test
    void rejectsManagerFromClientOrderHistory() {
        when(userRepositoryPort.findById(2)).thenReturn(Optional.of(TestOrderData.encargada()));

        assertThatThrownBy(() -> authorizationService.requireClienteForHistory(
                TestOrderData.authenticatedEncargada()
        ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Solo los clientes pueden consultar su historial de pedidos.");
    }
}
