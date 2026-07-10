package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOrderDetailUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private GetOrderDetailUseCase useCase;

    @Test
    void preventsClientFromReadingAnotherClientsOrder() {
        when(authorizationService.requireActiveUser(TestOrderData.authenticatedClient()))
                .thenReturn(TestOrderData.user(7, "Otro Cliente", UserRole.CLIENTE));
        when(orderRepositoryPort.findById(10)).thenReturn(Optional.of(TestOrderData.pendingOrder()));

        assertThatThrownBy(() -> useCase.getOrder(10, TestOrderData.authenticatedClient()))
                .isInstanceOf(ForbiddenException.class);

        verify(orderRepositoryPort, never()).findItemsByOrderId(10);
    }
}
