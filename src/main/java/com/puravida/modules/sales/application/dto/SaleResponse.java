package com.puravida.modules.sales.application.dto;

import com.puravida.modules.sales.domain.model.Sale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SaleResponse(
        Integer id,
        Integer pedidoId,
        String fuente,
        String estado,
        LocalDate fecha,
        LocalTime hora,
        BigDecimal total,
        Integer registradoPor,
        String observaciones,
        String motivoAnulacion,
        LocalDateTime anuladaEn,
        Integer usuarioAnuloId,
        LocalDateTime creadoEn
) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.id(),
                sale.orderId(),
                sale.source().databaseValue(),
                sale.status().databaseValue(),
                sale.fecha(),
                sale.hora(),
                sale.total(),
                sale.registradoPor(),
                sale.observaciones(),
                sale.motivoAnulacion(),
                sale.anuladaEn(),
                sale.usuarioAnuloId(),
                sale.creadoEn()
        );
    }
}
