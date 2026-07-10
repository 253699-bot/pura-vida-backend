package com.puravida.modules.menu.infrastructure.persistence;

import com.puravida.modules.menu.domain.model.Dish;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PLATILLOS")
public class DishEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_platillo")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "Descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "Tipo_platillo", nullable = false, columnDefinition = "ENUM('platillo_fuerte','bebida','complemento','postre')")
    private String tipoPlatillo;

    @Column(name = "Precio_base", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "Activo", nullable = false)
    private boolean activo;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "Actualizado_en")
    private LocalDateTime actualizadoEn;

    protected DishEntity() {
    }

    public Dish toDomain() {
        return new Dish(
                id,
                nombre,
                descripcion,
                tipoPlatillo,
                precioBase,
                activo,
                creadoEn,
                actualizadoEn
        );
    }
}
