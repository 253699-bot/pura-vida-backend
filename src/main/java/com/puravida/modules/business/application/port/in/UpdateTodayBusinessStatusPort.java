package com.puravida.modules.business.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.dto.UpdateTodayBusinessStatusRequest;

public interface UpdateTodayBusinessStatusPort {

    TodayBusinessStatusResponse updateToday(
            UpdateTodayBusinessStatusRequest request,
            AuthenticatedUser authenticatedUser
    );
}
