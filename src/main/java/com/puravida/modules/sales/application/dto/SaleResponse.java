package com.puravida.modules.sales.application.dto;

import com.puravida.modules.sales.domain.model.ManualSaleLine;
import com.puravida.modules.sales.domain.model.Sale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        LocalDateTime creadoEn,
        List<SaleItemResponse> items
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
                sale.creadoEn(),
                List.of()
        );
    }

    public static SaleResponse from(Sale sale, List<ManualSaleLine> lines) {
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
                sale.creadoEn(),
                lines.stream().map(SaleItemResponse::from).toList()
        );
    }

    public SaleResponse(
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
        this(id, pedidoId, fuente, estado, fecha, hora, total, registradoPor, observaciones,
                motivoAnulacion, anuladaEn, usuarioAnuloId, creadoEn, List.of());
    }
}
