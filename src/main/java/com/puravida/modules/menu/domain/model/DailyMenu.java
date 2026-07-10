package com.puravida.modules.menu.domain.model;

import java.time.LocalDate;
import java.util.List;

public record DailyMenu(
        LocalDate fecha,
        List<DailyMenuItem> items
) {
}
