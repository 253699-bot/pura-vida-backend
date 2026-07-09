package com.puravida.modules.users.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
