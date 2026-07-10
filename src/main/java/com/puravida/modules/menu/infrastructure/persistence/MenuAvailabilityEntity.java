package com.puravida.modules.menu.infrastructure.persistence;

import com.puravida.modules.menu.domain.model.MenuAvailability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "DISPONIBILIDAD_MENU")
public class MenuAvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_disponibilidad")
    private Integer id;

    @Column(name = "Id_menu", nullable = false, unique = true)
    private Integer menuId;

    @Column(name = "Disponible", nullable = false)
    private boolean disponible;

    @Column(name = "Hora_publicacion")
    private LocalTime horaPublicacion;

    @Column(name = "Hora_agotado")
    private LocalTime horaAgotado;

    @Column(name = "Actualizado_en")
    private LocalDateTime actualizadoEn;

    protected MenuAvailabilityEntity() {
    }

    private MenuAvailabilityEntity(
            Integer id,
            Integer menuId,
            boolean disponible,
            LocalTime horaPublicacion,
            LocalTime horaAgotado,
            LocalDateTime actualizadoEn
    ) {
        this.id = id;
        this.menuId = menuId;
        this.disponible = disponible;
        this.horaPublicacion = horaPublicacion;
        this.horaAgotado = horaAgotado;
        this.actualizadoEn = actualizadoEn;
    }

    public static MenuAvailabilityEntity available(Integer menuId) {
        return new MenuAvailabilityEntity(null, menuId, true, null, null, null);
    }

    public void changeAvailability(boolean disponible) {
        this.disponible = disponible;
        this.horaAgotado = disponible ? null : LocalTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    public Integer menuId() {
        return menuId;
    }

    public MenuAvailability toDomain() {
        return new MenuAvailability(
                id,
                menuId,
                disponible,
                horaPublicacion,
                horaAgotado,
                actualizadoEn
        );
    }
}
