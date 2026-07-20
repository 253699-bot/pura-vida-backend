package com.puravida.modules.business.application.port.out;

import com.puravida.modules.business.domain.model.ActiveBusinessOrderCounts;
import java.time.LocalDateTime;

public interface ActiveBusinessOrdersPort {

    ActiveBusinessOrderCounts countActiveSince(LocalDateTime cycleStartedAt);
}