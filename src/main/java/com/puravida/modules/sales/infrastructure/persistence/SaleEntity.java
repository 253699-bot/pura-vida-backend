package com.puravida.modules.sales.infrastructure.persistence;

import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "VENTAS")
public class SaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_venta")
    private Integer id;

    @Column(name = "Id_pedido", unique = true)
    private Integer orderId;

    @Convert(converter = SaleSourceConverter.class)
    @Column(name = "Fuente", nullable = false, columnDefinition = "ENUM('manual_fonda','remota')")
    private SaleSource source;

    @Convert(converter = SaleStatusConverter.class)
    @Column(name = "Estado", nullable = false, columnDefinition = "ENUM('activa','anulada')")
    private SaleStatus status;

    @Column(name = "Clave_idempotencia", length = 100)
    private String idempotencyKey;

    @Column(name = "Fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "Hora", nullable = false)
    private LocalTime hora;

    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "Registrado_por")
    private Integer registradoPor;

    @Column(name = "Observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "Motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "Anulada_en")
    private LocalDateTime anuladaEn;

    @Column(name = "Id_usuario_anulo")
    private Integer usuarioAnuloId;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected SaleEntity() {
    }

    private SaleEntity(
            Integer id,
            Integer orderId,
            SaleSource source,
            SaleStatus status,
            String idempotencyKey,
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
        this.id = id;
        this.orderId = orderId;
        this.source = source;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.fecha = fecha;
        this.hora = hora;
        this.total = total;
        this.registradoPor = registradoPor;
        this.observaciones = observaciones;
        this.motivoAnulacion = motivoAnulacion;
        this.anuladaEn = anuladaEn;
        this.usuarioAnuloId = usuarioAnuloId;
        this.creadoEn = creadoEn;
    }

    public static SaleEntity fromDomain(Sale sale) {
        return new SaleEntity(
                sale.id(),
                sale.orderId(),
                sale.source(),
                sale.status(),
                sale.idempotencyKey(),
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

    public Sale toDomain() {
        return new Sale(
                id,
                orderId,
                source,
                status,
                idempotencyKey,
                fecha,
                hora,
                total,
                registradoPor,
                observaciones,
                motivoAnulacion,
                anuladaEn,
                usuarioAnuloId,
                creadoEn
        );
    }
}
