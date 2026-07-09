package com.puravida.modules.users.application.port.out;

import com.puravida.modules.users.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    User save(User user);
}
