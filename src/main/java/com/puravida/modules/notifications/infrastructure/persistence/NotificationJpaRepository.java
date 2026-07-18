package com.puravida.modules.notifications.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Integer> {

    List<NotificationEntity> findByUsuarioIdOrderByCreadoEnDescIdDesc(Integer usuarioId);

    Optional<NotificationEntity> findByIdAndUsuarioId(Integer id, Integer usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NotificationEntity entity
            SET entity.estado = 'leida', entity.leidaEn = CURRENT_TIMESTAMP
            WHERE entity.usuarioId = :usuarioId AND entity.estado = 'no_leida'
            """)
    int markAllUnreadByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
