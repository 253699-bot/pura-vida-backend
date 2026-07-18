package com.puravida.modules.users.infrastructure.persistence;

import com.puravida.modules.users.domain.model.UserRole;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM UserEntity entity WHERE entity.id = :id")
    Optional<UserEntity> findByIdForUpdate(@Param("id") Integer id);

    Optional<UserEntity> findByCorreo(String correo);

    List<UserEntity> findByRolAndActivoTrueAndNotificacionesActivasTrue(UserRole role);

    boolean existsByCorreo(String correo);
}
