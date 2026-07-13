package com.puravida.modules.sales.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.SaleResponse;
import com.puravida.modules.sales.application.port.in.GetSalesPort;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.SaleSearchCriteria;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSalesUseCase implements GetSalesPort {

    private final SaleRepositoryPort saleRepositoryPort;
    private final SalesAuthorizationService authorizationService;

    public GetSalesUseCase(
            SaleRepositoryPort saleRepositoryPort,
            SalesAuthorizationService authorizationService
    ) {
        this.saleRepositoryPort = saleRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSales(
            String from,
            String to,
            String source,
            String status,
            AuthenticatedUser authenticatedUser
    ) {
        authorizationService.requireEncargada(authenticatedUser);
        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new SaleValidationException("La fecha from no puede ser posterior a to.");
        }

        SaleSearchCriteria criteria = new SaleSearchCriteria(
                fromDate,
                toDate,
                parseSource(source),
                parseStatus(status)
        );
        return saleRepositoryPort.findAll(criteria).stream().map(SaleResponse::from).toList();
    }

    private LocalDate parseDate(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new SaleValidationException("La fecha " + field + " debe usar formato YYYY-MM-DD.");
        }
    }

    private SaleSource parseSource(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return SaleSource.fromDatabaseValue(value.trim().toLowerCase(Locale.ROOT));
    }

    private SaleStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return SaleStatus.ACTIVA;
        }
        if ("all".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return SaleStatus.fromDatabaseValue(value.trim().toLowerCase(Locale.ROOT));
    }
}
