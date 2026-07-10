package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.menu.application.dto.TodayMenuResponse;
import com.puravida.modules.menu.application.port.in.GetTodayMenuPort;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTodayMenuUseCase implements GetTodayMenuPort {

    private final DailyMenuRepositoryPort dailyMenuRepositoryPort;

    public GetTodayMenuUseCase(DailyMenuRepositoryPort dailyMenuRepositoryPort) {
        this.dailyMenuRepositoryPort = dailyMenuRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TodayMenuResponse getToday() {
        LocalDate today = LocalDate.now();
        var items = dailyMenuRepositoryPort.findByFecha(today);

        if (items.isEmpty()) {
            return TodayMenuResponse.notConfigured(today);
        }

        return TodayMenuResponse.configured(today, items);
    }
}
