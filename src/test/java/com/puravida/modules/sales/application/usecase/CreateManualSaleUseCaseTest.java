package com.puravida.modules.sales.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.Sale;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateManualSaleUseCaseTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    @Mock
    private SalesAuthorizationService authorizationService;

    @InjectMocks
    private CreateManualSaleUseCase useCase;

    @Test
    void createsActiveManualSaleWithoutOrder() {
        when(authorizationService.requireEncargada(TestSaleData.authenticatedEncargada()))
                .thenReturn(TestSaleData.encargada());
        when(saleRepositoryPort.save(any(Sale.class))).thenReturn(TestSaleData.activeManualSale());

        var response = useCase.create(
                new CreateManualSaleRequest(new BigDecimal("125.00"), " Venta en mostrador "),
                TestSaleData.authenticatedEncargada()
        );

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepositoryPort).save(saleCaptor.capture());
        assertThat(saleCaptor.getValue().orderId()).isNull();
        assertThat(saleCaptor.getValue().source().databaseValue()).isEqualTo("manual_fonda");
        assertThat(saleCaptor.getValue().status().databaseValue()).isEqualTo("activa");
        assertThat(saleCaptor.getValue().observaciones()).isEqualTo("Venta en mostrador");
        assertThat(response.id()).isEqualTo(20);
    }

    @Test
    void rejectsNonPositiveTotal() {
        when(authorizationService.requireEncargada(TestSaleData.authenticatedEncargada()))
                .thenReturn(TestSaleData.encargada());

        assertThatThrownBy(() -> useCase.create(
                new CreateManualSaleRequest(BigDecimal.ZERO, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class);

        verify(saleRepositoryPort, never()).save(any());
    }
}
