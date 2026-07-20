package com.puravida.modules.sales.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.dto.SaleResponse;

public interface CreateManualSalePort {

    SaleResponse create(
            String idempotencyKey,
            CreateManualSaleRequest request,
            AuthenticatedUser authenticatedUser
    );
}
