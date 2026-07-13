package com.puravida.modules.sales.domain.model;

import java.time.LocalDate;

public record SaleSearchCriteria(
        LocalDate from,
        LocalDate to,
        SaleSource source,
        SaleStatus status
) {
}
