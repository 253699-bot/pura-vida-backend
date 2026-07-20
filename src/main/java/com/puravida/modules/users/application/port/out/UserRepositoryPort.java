package com.puravida.modules.users.application.port.out;

import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(Integer id);

    Optional<User> findByIdForUpdate(Integer id);

    Optional<User> findByCorreo(String correo);

    List<User> findActiveWithNotificationsByRole(UserRole role);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Integer id);

    User save(User user);
}
