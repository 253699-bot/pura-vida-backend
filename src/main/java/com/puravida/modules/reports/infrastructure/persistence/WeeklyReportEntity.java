package com.puravida.modules.reports.infrastructure.persistence;

import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "REPORTES_SEMANALES")
public class WeeklyReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_reporte")
    private Integer id;

    @Column(name = "Semana_inicio", nullable = false)
    private LocalDate semanaInicio;

    @Column(name = "Semana_fin", nullable = false)
    private LocalDate semanaFin;

    @Column(name = "Total_pedidos_app", nullable = false)
    private Integer totalPedidosApp;

    @Column(name = "Total_ingresos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIngresos;

    @Column(name = "Id_platillo_mas_vendido")
    private Integer platilloMasVendidoId;

    @Column(name = "Dia_mayor_demanda")
    private LocalDate diaMayorDemanda;

    @Column(name = "Ruta_archivo", columnDefinition = "TEXT")
    private String rutaArchivo;

    @Column(name = "Resumen_json", columnDefinition = "json")
    private String resumenJson;

    @Column(name = "Version_formato", nullable = false)
    private Integer versionFormato;

    @Column(name = "Generado_por")
    private Integer generadoPor;

    @Column(name = "Generado_en", nullable = false)
    private LocalDateTime generadoEn;

    protected WeeklyReportEntity() {
    }


    public static WeeklyReportEntity fromDomain(StoredWeeklyReport report) {
        WeeklyReportEntity entity = new WeeklyReportEntity();
        entity.id = report.id();
        entity.semanaInicio = report.semanaInicio();
        entity.semanaFin = report.semanaFin();
        entity.totalPedidosApp = report.totalPedidosApp();
        entity.totalIngresos = report.totalIngresos();
        entity.platilloMasVendidoId = report.platilloMasVendidoId();
        entity.diaMayorDemanda = report.diaMayorDemanda();
        entity.rutaArchivo = report.rutaArchivo();
        entity.resumenJson = report.resumenJson();
        entity.versionFormato = report.versionFormato();
        entity.generadoPor = report.generadoPor();
        entity.generadoEn = report.generadoEn();
        return entity;
    }
    public StoredWeeklyReport toDomain() {
        return new StoredWeeklyReport(
                id, semanaInicio, semanaFin, totalPedidosApp, totalIngresos,
                platilloMasVendidoId, diaMayorDemanda, rutaArchivo, resumenJson,
                versionFormato, generadoPor, generadoEn
        );
    }
}
