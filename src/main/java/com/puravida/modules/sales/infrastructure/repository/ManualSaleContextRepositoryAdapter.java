package com.puravida.modules.sales.infrastructure.repository;

import com.puravida.modules.sales.application.port.out.ManualSaleContextRepositoryPort;
import com.puravida.modules.sales.domain.model.ManualSaleMenuItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ManualSaleContextRepositoryAdapter implements ManualSaleContextRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Boolean> lockBusinessOpenByDate(LocalDate date) {
        List<?> rows = entityManager.createNativeQuery("""
                        SELECT status.Abierto
                        FROM ESTADO_DIA status
                        WHERE status.Fecha = :date
                        FOR UPDATE
                        """)
                .setParameter("date", date)
                .getResultList();
        return rows.stream().findFirst().map(this::toBoolean);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ManualSaleMenuItem> lockMenuItemsByIds(List<Integer> menuItemIds) {
        if (menuItemIds.isEmpty()) {
            return List.of();
        }

        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT menu.Id_menu,
                               menu.Fecha,
                               menu.Id_platillo,
                               dish.Nombre,
                               menu.Precio_dia,
                               menu.Publicado,
                               dish.Activo,
                               COALESCE(availability.Disponible, TRUE)
                        FROM MENU_DIA menu
                        JOIN PLATILLOS dish ON dish.Id_platillo = menu.Id_platillo
                        LEFT JOIN DISPONIBILIDAD_MENU availability ON availability.Id_menu = menu.Id_menu
                        WHERE menu.Id_menu IN (:menuItemIds)
                        ORDER BY menu.Id_menu
                        FOR UPDATE
                        """)
                .setParameter("menuItemIds", menuItemIds)
                .getResultList();

        return rows.stream().map(this::toDomain).toList();
    }

    private ManualSaleMenuItem toDomain(Object[] row) {
        return new ManualSaleMenuItem(
                ((Number) row[0]).intValue(),
                toLocalDate(row[1]),
                ((Number) row[2]).intValue(),
                (String) row[3],
                (BigDecimal) row[4],
                toBoolean(row[5]),
                toBoolean(row[6]),
                toBoolean(row[7])
        );
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return ((Date) value).toLocalDate();
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return ((Number) value).intValue() != 0;
    }
}
