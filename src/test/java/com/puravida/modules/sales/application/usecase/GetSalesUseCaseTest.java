package com.puravida.modules.sales.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.SaleSearchCriteria;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSalesUseCaseTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    @Mock
    private SalesAuthorizationService authorizationService;

    @InjectMocks
    private GetSalesUseCase useCase;

    @Test
    void defaultsToActiveSalesAndParsesFilters() {
        when(saleRepositoryPort.findAll(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(TestSaleData.activeManualSale()));

        var response = useCase.getSales(
                "2026-07-01",
                "2026-07-31",
                "manual_fonda",
                null,
                TestSaleData.authenticatedEncargada()
        );

        ArgumentCaptor<SaleSearchCriteria> criteria = ArgumentCaptor.forClass(SaleSearchCriteria.class);
        verify(saleRepositoryPort).findAll(criteria.capture());
        assertThat(criteria.getValue().from()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(criteria.getValue().to()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(criteria.getValue().source()).isEqualTo(SaleSource.MANUAL_FONDA);
        assertThat(criteria.getValue().status()).isEqualTo(SaleStatus.ACTIVA);
        assertThat(response).hasSize(1);
    }

    @Test
    void rejectsInvertedDateRange() {
        assertThatThrownBy(() -> useCase.getSales(
                "2026-07-31",
                "2026-07-01",
                "all",
                "all",
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class);
    }
}
