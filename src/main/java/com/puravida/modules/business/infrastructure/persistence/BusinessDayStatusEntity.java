package com.puravida.modules.business.infrastructure.persistence;

import com.puravida.modules.business.domain.model.BusinessDayStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ESTADO_DIA")
public class BusinessDayStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_estado")
    private Integer id;

    @Column(name = "Fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "Abierto", nullable = false)
    private boolean abierto;

    @Column(name = "Motivo_cierre", columnDefinition = "TEXT")
    private String motivoCierre;

    @Column(name = "Registrado_por", nullable = false)
    private Integer registradoPor;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "Actualizado_en")
    private LocalDateTime actualizadoEn;

    protected BusinessDayStatusEntity() {
    }

    private BusinessDayStatusEntity(
            Integer id,
            LocalDate fecha,
            boolean abierto,
            String motivoCierre,
            Integer registradoPor,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        this.id = id;
        this.fecha = fecha;
        this.abierto = abierto;
        this.motivoCierre = motivoCierre;
        this.registradoPor = registradoPor;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static BusinessDayStatusEntity fromDomain(BusinessDayStatus status) {
        return new BusinessDayStatusEntity(
                status.id(),
                status.fecha(),
                status.abierto(),
                status.motivoCierre(),
                status.registradoPor(),
                status.creadoEn(),
                status.actualizadoEn()
        );
    }

    public BusinessDayStatus toDomain() {
        return new BusinessDayStatus(
                id,
                fecha,
                abierto,
                motivoCierre,
                registradoPor,
                creadoEn,
                actualizadoEn
        );
    }
}
