package com.puravida.modules.menu.infrastructure.persistence;

import com.puravida.modules.menu.domain.model.DailyMenuItem;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.domain.model.MenuAvailability;
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
@Table(name = "MENU_DIA")
public class DailyMenuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_menu")
    private Integer id;

    @Column(name = "Fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "Id_platillo", nullable = false)
    private Integer dishId;

    @Column(name = "Precio_dia", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioDia;

    @Column(name = "Creado_por", nullable = false)
    private Integer creadoPor;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected DailyMenuEntity() {
    }

    private DailyMenuEntity(
            Integer id,
            LocalDate fecha,
            Integer dishId,
            BigDecimal precioDia,
            Integer creadoPor,
            LocalDateTime creadoEn
    ) {
        this.id = id;
        this.fecha = fecha;
        this.dishId = dishId;
        this.precioDia = precioDia;
        this.creadoPor = creadoPor;
        this.creadoEn = creadoEn;
    }

    public static DailyMenuEntity newItem(LocalDate fecha, Dish dish, Integer creadoPor) {
        return new DailyMenuEntity(
                null,
                fecha,
                dish.id(),
                dish.precioBase(),
                creadoPor,
                LocalDateTime.now()
        );
    }

    public Integer id() {
        return id;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public Integer dishId() {
        return dishId;
    }

    public BigDecimal precioDia() {
        return precioDia;
    }

    public DailyMenuItem toDomain(Dish dish, MenuAvailability availability) {
        return new DailyMenuItem(
                id,
                fecha,
                dish,
                precioDia,
                creadoPor,
                creadoEn,
                availability
        );
    }
}
