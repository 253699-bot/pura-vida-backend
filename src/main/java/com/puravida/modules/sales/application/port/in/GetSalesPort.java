package com.puravida.modules.sales.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.sales.application.dto.SaleResponse;
import java.util.List;

public interface GetSalesPort {

    List<SaleResponse> getSales(
            String from,
            String to,
            String source,
            String status,
            AuthenticatedUser authenticatedUser
    );
}
