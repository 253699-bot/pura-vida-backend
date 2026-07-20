package com.puravida.modules.sales.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import com.puravida.modules.sales.domain.model.ManualSaleLine;
import com.puravida.modules.sales.domain.model.ManualSaleMenuItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

final class TestSaleData {

    private TestSaleData() {
    }

    static AuthenticatedUser authenticatedEncargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }

    static User encargada() {
        return new User(
                4,
                "Encargada",
                "encargada@example.com",
                null,
                "hash",
                UserRole.ENCARGADA,
                null,
                true,
                true,
                LocalDateTime.of(2026, 7, 11, 8, 0),
                null
        );
    }

    static Sale activeManualSale() {
        return new Sale(
                20,
                null,
                SaleSource.MANUAL_FONDA,
                SaleStatus.ACTIVA,
                "manual-key",
                LocalDate.of(2026, 7, 11),
                LocalTime.of(12, 0),
                new BigDecimal("125.00"),
                4,
                "Venta en mostrador",
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 11, 12, 0)
        );
    }

    static Sale cancelledManualSale() {
        Sale sale = activeManualSale();
        return new Sale(
                sale.id(),
                sale.orderId(),
                sale.source(),
                SaleStatus.ANULADA,
                sale.idempotencyKey(),
                sale.fecha(),
                sale.hora(),
                sale.total(),
                sale.registradoPor(),
                sale.observaciones(),
                "Captura duplicada",
                LocalDateTime.of(2026, 7, 11, 12, 30),
                4,
                sale.creadoEn()
        );
    }

    static Sale activeRemoteSale() {
        return new Sale(
                21,
                10,
                SaleSource.REMOTA,
                SaleStatus.ACTIVA,
                null,
                LocalDate.of(2026, 7, 11),
                LocalTime.of(12, 0),
                new BigDecimal("125.00"),
                4,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 11, 12, 0)
        );
    }

    static ManualSaleMenuItem menuItem(
            Integer id,
            boolean published,
            boolean active,
            boolean available
    ) {
        return new ManualSaleMenuItem(
                id,
                LocalDate.now(),
                id + 100,
                "Platillo " + id,
                new BigDecimal("25.00"),
                published,
                active,
                available
        );
    }

    static ManualSaleLine storedLine() {
        return new ManualSaleLine(
                30,
                20,
                110,
                10,
                "Platillo 10",
                5,
                new BigDecimal("25.00"),
                new BigDecimal("125.00")
        );
    }
}
