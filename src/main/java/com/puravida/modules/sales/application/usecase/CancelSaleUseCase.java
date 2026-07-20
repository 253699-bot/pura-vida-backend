package com.puravida.modules.sales.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.CancelSaleRequest;
import com.puravida.modules.sales.application.dto.SaleResponse;
import com.puravida.modules.sales.application.port.in.CancelSalePort;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelSaleUseCase implements CancelSalePort {

    private final SaleRepositoryPort saleRepositoryPort;
    private final SalesAuthorizationService authorizationService;

    public CancelSaleUseCase(
            SaleRepositoryPort saleRepositoryPort,
            SalesAuthorizationService authorizationService
    ) {
        this.saleRepositoryPort = saleRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public SaleResponse cancel(
            Integer saleId,
            CancelSaleRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        String reason = normalizeReason(request);
        Sale sale = saleRepositoryPort.findByIdForUpdate(saleId)
                .orElseThrow(() -> new NotFoundException("No se encontro la venta."));
        if (sale.source() == SaleSource.REMOTA) {
            throw new ConflictException("Las ventas remotas se cancelan desde el pedido.");
        }
        if (sale.status() != SaleStatus.ACTIVA) {
            throw new ConflictException("Solo las ventas activas pueden anularse.");
        }

        return SaleResponse.from(saleRepositoryPort.save(sale.cancel(reason, actor.id())));
    }

    private String normalizeReason(CancelSaleRequest request) {
        if (request == null || request.motivo() == null || request.motivo().isBlank()) {
            throw new SaleValidationException("El motivo de anulacion es obligatorio.");
        }
        return request.motivo().trim();
    }
}
