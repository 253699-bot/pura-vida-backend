package com.puravida.modules.sales.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.sales.application.dto.CreateManualSaleItemRequest;
import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.port.out.ManualSaleContextRepositoryPort;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.ManualSaleLine;
import com.puravida.modules.sales.domain.model.ManualSaleMenuItem;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private ManualSaleContextRepositoryPort contextRepositoryPort;

    @Mock
    private SalesAuthorizationService authorizationService;

    @InjectMocks
    private CreateManualSaleUseCase useCase;

    @Test
    void createsManualSaleFromDailyPricesAndPersistsSnapshots() {
        authorize();
        ManualSaleMenuItem menuItem = TestSaleData.menuItem(10, true, true, true);
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(true));
        when(contextRepositoryPort.lockMenuItemsByIds(List.of(10))).thenReturn(List.of(menuItem));
        when(saleRepositoryPort.save(any(Sale.class))).thenReturn(TestSaleData.activeManualSale());
        when(saleRepositoryPort.saveLines(any())).thenReturn(List.of(TestSaleData.storedLine()));

        var response = useCase.create(
                "  manual-key  ",
                request(5, " Venta en mostrador "),
                TestSaleData.authenticatedEncargada()
        );

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepositoryPort).save(saleCaptor.capture());
        assertThat(saleCaptor.getValue().total()).isEqualByComparingTo("125.00");
        assertThat(saleCaptor.getValue().idempotencyKey()).isEqualTo("manual-key");
        assertThat(saleCaptor.getValue().observaciones()).isEqualTo("Venta en mostrador");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ManualSaleLine>> linesCaptor = ArgumentCaptor.forClass(List.class);
        verify(saleRepositoryPort).saveLines(linesCaptor.capture());
        assertThat(linesCaptor.getValue().get(0).saleId()).isEqualTo(20);
        assertThat(linesCaptor.getValue().get(0).unitPrice()).isEqualByComparingTo("25.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).menuItemId()).isEqualTo(10);
    }

    @Test
    void rejectsDuplicateMenuItems() {
        authorize();
        CreateManualSaleRequest request = new CreateManualSaleRequest(
                List.of(new CreateManualSaleItemRequest(10, 1), new CreateManualSaleItemRequest(10, 2)),
                null
        );

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request,
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class)
                .hasMessageContaining("duplicados");

        verify(contextRepositoryPort, never()).lockBusinessOpenByDate(any());
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsClosedBusiness() {
        authorize();
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request(1, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("cerrada");

        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsUnconfiguredBusinessDay() {
        authorize();
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request(1, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("configurado");
    }

    @Test
    void rejectsUnpublishedMenuItem() {
        assertMenuStateRejected(TestSaleData.menuItem(10, false, true, true), "publicado");
    }

    @Test
    void rejectsInactiveDish() {
        assertMenuStateRejected(TestSaleData.menuItem(10, true, false, true), "inactivo");
    }

    @Test
    void rejectsUnavailableMenuItem() {
        assertMenuStateRejected(TestSaleData.menuItem(10, true, true, false), "disponible");
    }

    @Test
    void returnsExistingSaleForSameActorKeyAndLogicalPayload() {
        authorize();
        when(saleRepositoryPort.findByActorAndIdempotencyKey(4, "manual-key"))
                .thenReturn(Optional.of(TestSaleData.activeManualSale()));
        when(saleRepositoryPort.findLinesBySaleId(20))
                .thenReturn(List.of(TestSaleData.storedLine()));

        var response = useCase.create(
                "manual-key",
                request(5, " Venta en mostrador "),
                TestSaleData.authenticatedEncargada()
        );

        assertThat(response.id()).isEqualTo(20);
        assertThat(response.items()).hasSize(1);
        verify(contextRepositoryPort, never()).lockBusinessOpenByDate(any());
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsSameActorKeyWithDifferentLogicalPayload() {
        authorize();
        when(saleRepositoryPort.findByActorAndIdempotencyKey(4, "manual-key"))
                .thenReturn(Optional.of(TestSaleData.activeManualSale()));
        when(saleRepositoryPort.findLinesBySaleId(20))
                .thenReturn(List.of(TestSaleData.storedLine()));

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request(4, "Venta en mostrador"),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("otro payload");

        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rechecksIdempotencyAfterStateAndMenuLocks() {
        authorize();
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(true));
        when(contextRepositoryPort.lockMenuItemsByIds(List.of(10)))
                .thenReturn(List.of(TestSaleData.menuItem(10, true, true, true)));
        when(saleRepositoryPort.findByActorAndIdempotencyKeyForUpdate(4, "manual-key"))
                .thenReturn(Optional.of(TestSaleData.activeManualSale()));
        when(saleRepositoryPort.findLinesBySaleId(20))
                .thenReturn(List.of(TestSaleData.storedLine()));

        var response = useCase.create(
                "manual-key",
                request(5, "Venta en mostrador"),
                TestSaleData.authenticatedEncargada()
        );

        assertThat(response.id()).isEqualTo(20);
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void propagatesDetailFailureInsideTransactionalBoundary() {
        authorize();
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(true));
        when(contextRepositoryPort.lockMenuItemsByIds(List.of(10)))
                .thenReturn(List.of(TestSaleData.menuItem(10, true, true, true)));
        when(saleRepositoryPort.save(any(Sale.class))).thenReturn(TestSaleData.activeManualSale());
        when(saleRepositoryPort.saveLines(any())).thenThrow(new RuntimeException("detail insert failed"));

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request(5, "Venta en mostrador"),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(RuntimeException.class)
                .hasMessage("detail insert failed");

        verify(saleRepositoryPort).save(any(Sale.class));
        verify(saleRepositoryPort).saveLines(any());
    }

    @Test
    void rejectsSubtotalOutsideDecimalTenTwoRange() {
        authorize();
        ManualSaleMenuItem expensiveItem = new ManualSaleMenuItem(
                10, LocalDate.now(), 110, "Platillo", new BigDecimal("999999.99"), true, true, true);
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(true));
        when(contextRepositoryPort.lockMenuItemsByIds(List.of(10))).thenReturn(List.of(expensiveItem));

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request(101, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class)
                .hasMessageContaining("subtotal")
                .hasMessageContaining("DECIMAL(10,2)");

        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsTotalOutsideDecimalTenTwoRange() {
        authorize();
        ManualSaleMenuItem first = new ManualSaleMenuItem(
                10, LocalDate.now(), 110, "Uno", new BigDecimal("600000.00"), true, true, true);
        ManualSaleMenuItem second = new ManualSaleMenuItem(
                11, LocalDate.now(), 111, "Dos", new BigDecimal("600000.00"), true, true, true);
        CreateManualSaleRequest request = new CreateManualSaleRequest(List.of(
                new CreateManualSaleItemRequest(10, 100),
                new CreateManualSaleItemRequest(11, 100)
        ), null);
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(true));
        when(contextRepositoryPort.lockMenuItemsByIds(List.of(10, 11))).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request,
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class)
                .hasMessageContaining("total")
                .hasMessageContaining("DECIMAL(10,2)");

        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsMissingOrNonAsciiIdempotencyKey() {
        authorize();
        assertThatThrownBy(() -> useCase.create(
                null,
                request(1, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class).hasMessageContaining("obligatorio");

        assertThatThrownBy(() -> useCase.create(
                "clave-ñ",
                request(1, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(SaleValidationException.class).hasMessageContaining("ASCII");
    }

    private void assertMenuStateRejected(ManualSaleMenuItem menuItem, String message) {
        authorize();
        when(contextRepositoryPort.lockBusinessOpenByDate(any())).thenReturn(Optional.of(true));
        when(contextRepositoryPort.lockMenuItemsByIds(List.of(10))).thenReturn(List.of(menuItem));

        assertThatThrownBy(() -> useCase.create(
                "manual-key",
                request(1, null),
                TestSaleData.authenticatedEncargada()
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining(message);

        verify(saleRepositoryPort, never()).save(any());
    }

    private void authorize() {
        when(authorizationService.requireEncargada(TestSaleData.authenticatedEncargada()))
                .thenReturn(TestSaleData.encargada());
    }

    private CreateManualSaleRequest request(int quantity, String observations) {
        return new CreateManualSaleRequest(
                List.of(new CreateManualSaleItemRequest(10, quantity)),
                observations
        );
    }
}
