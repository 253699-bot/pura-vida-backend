package com.puravida.modules.sales.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.CancelSaleRequest;
import com.puravida.modules.sales.application.dto.SaleResponse;

public interface CancelSalePort {

    SaleResponse cancel(Integer saleId, CancelSaleRequest request, AuthenticatedUser authenticatedUser);
}
