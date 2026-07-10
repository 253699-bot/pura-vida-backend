package com.puravida.modules.business.application.usecase;

import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.port.in.GetTodayBusinessStatusPort;
import com.puravida.modules.business.application.port.out.BusinessDayStatusRepositoryPort;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTodayBusinessStatusUseCase implements GetTodayBusinessStatusPort {

    private final BusinessDayStatusRepositoryPort repositoryPort;

    public GetTodayBusinessStatusUseCase(BusinessDayStatusRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TodayBusinessStatusResponse getToday() {
        LocalDate today = LocalDate.now();

        return repositoryPort.findByFecha(today)
                .map(TodayBusinessStatusResponse::from)
                .orElseGet(() -> TodayBusinessStatusResponse.notConfigured(today));
    }
}
