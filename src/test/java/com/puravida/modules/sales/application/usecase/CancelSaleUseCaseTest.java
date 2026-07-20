package com.puravida.modules.sales.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.sales.application.dto.CancelSaleRequest;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.shared.domain.exception.ConflictException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelSaleUseCaseTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    @Mock
    private SalesAuthorizationService authorizationService;

    @InjectMocks
    private CancelSaleUseCase useCase;

    @Test
    void cancelsActiveSaleWithAuditData() {
        when(authorizationService.requireEncargada(TestSaleData.authenticatedEncargada()))
                .thenReturn(TestSaleData.encargada());
        when(saleRepositoryPort.findByIdForUpdate(20)).thenReturn(Optional.of(TestSaleData.activeManualSale()));
        when(saleRepositoryPort.save(any(Sale.class))).thenReturn(TestSaleData.cancelledManualSale());

        var response = useCase.cancel(
                20,
                new CancelSaleRequest(" Captura duplicada "),
                TestSaleData.authenticatedEncargada()
        );

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepositoryPort).save(saleCaptor.capture());
        assertThat(saleCaptor.getValue().status().databaseValue()).isEqualTo("anulada");
        assertThat(saleCaptor.getValue().motivoAnulacion()).isEqualTo("Captura duplicada");
        assertThat(saleCaptor.getValue().usuarioAnuloId()).isEqualTo(4);
        assertThat(response.estado()).isEqualTo("anulada");
    }

    @Test
    void rejectsAlreadyCancelledSale() {
        when(authorizationService.requireEncargada(TestSaleData.authenticatedEncargada()))
                .thenReturn(TestSaleData.encargada());
        when(saleRepositoryPort.findByIdForUpdate(20)).thenReturn(Optional.of(TestSaleData.cancelledManualSale()));

        assertThatThrownBy(() -> useCase.cancel(
                20,
                new CancelSaleRequest("Otro motivo"),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(ConflictException.class);

        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsRemoteSaleBecauseItMustBeCancelledFromOrder() {
        when(authorizationService.requireEncargada(TestSaleData.authenticatedEncargada()))
                .thenReturn(TestSaleData.encargada());
        when(saleRepositoryPort.findByIdForUpdate(21))
                .thenReturn(Optional.of(TestSaleData.activeRemoteSale()));

        assertThatThrownBy(() -> useCase.cancel(
                21,
                new CancelSaleRequest("No aplica"),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("pedido");

        verify(saleRepositoryPort, never()).save(any());
    }
}
