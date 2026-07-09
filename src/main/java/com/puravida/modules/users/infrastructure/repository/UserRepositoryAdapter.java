package com.puravida.modules.users.infrastructure.repository;

import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.infrastructure.persistence.UserEntity;
import com.puravida.modules.users.infrastructure.persistence.UserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByCorreo(String correo) {
        return userJpaRepository.findByCorreo(correo).map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return userJpaRepository.existsByCorreo(correo);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.fromDomain(user)).toDomain();
    }
}
