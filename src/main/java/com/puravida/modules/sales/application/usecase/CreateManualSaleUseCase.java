package com.puravida.modules.sales.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.dto.SaleResponse;
import com.puravida.modules.sales.application.port.in.CreateManualSalePort;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.users.domain.model.User;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateManualSaleUseCase implements CreateManualSalePort {

    private final SaleRepositoryPort saleRepositoryPort;
    private final SalesAuthorizationService authorizationService;

    public CreateManualSaleUseCase(
            SaleRepositoryPort saleRepositoryPort,
            SalesAuthorizationService authorizationService
    ) {
        this.saleRepositoryPort = saleRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public SaleResponse create(CreateManualSaleRequest request, AuthenticatedUser authenticatedUser) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        BigDecimal total = validateTotal(request);
        Sale sale = Sale.createManual(total, actor.id(), normalizeObservations(request.observaciones()));
        return SaleResponse.from(saleRepositoryPort.save(sale));
    }

    private BigDecimal validateTotal(CreateManualSaleRequest request) {
        if (request == null || request.total() == null || request.total().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SaleValidationException("El total debe ser mayor a cero.");
        }
        return request.total();
    }

    private String normalizeObservations(String observations) {
        return observations == null || observations.isBlank() ? null : observations.trim();
    }
}
