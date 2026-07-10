package com.puravida.modules.business.application.port.out;

import com.puravida.modules.business.domain.model.BusinessDayStatus;
import java.time.LocalDate;
import java.util.Optional;

public interface BusinessDayStatusRepositoryPort {

    Optional<BusinessDayStatus> findByFecha(LocalDate fecha);

    BusinessDayStatus save(BusinessDayStatus status);
}
