package com.puravida.modules.users.infrastructure.persistence;

import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIOS")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_usuario")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "Correo", nullable = false, unique = true, length = 255)
    private String correo;

    @Column(name = "Telefono", length = 20)
    private String telefono;

    @Column(name = "Password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Convert(converter = UserRoleConverter.class)
    @Column(name = "Rol", nullable = false, columnDefinition = "ENUM('cliente','encargada')")
    private UserRole rol;

    @Column(name = "Icono_perfil", columnDefinition = "TEXT")
    private String iconoPerfil;

    @Column(name = "Notificaciones_act", nullable = false)
    private boolean notificacionesActivas;

    @Column(name = "Activo", nullable = false)
    private boolean activo;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "Actualizado_en")
    private LocalDateTime actualizadoEn;

    protected UserEntity() {
    }

    private UserEntity(
            Integer id,
            String nombre,
            String correo,
            String telefono,
            String passwordHash,
            UserRole rol,
            String iconoPerfil,
            boolean notificacionesActivas,
            boolean activo,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.iconoPerfil = iconoPerfil;
        this.notificacionesActivas = notificacionesActivas;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static UserEntity fromDomain(User user) {
        return new UserEntity(
                user.id(),
                user.nombre(),
                user.correo(),
                user.telefono(),
                user.passwordHash(),
                user.rol(),
                user.iconoPerfil(),
                user.notificacionesActivas(),
                user.activo(),
                user.creadoEn(),
                user.actualizadoEn()
        );
    }

    public User toDomain() {
        return new User(
                id,
                nombre,
                correo,
                telefono,
                passwordHash,
                rol,
                iconoPerfil,
                notificacionesActivas,
                activo,
                creadoEn,
                actualizadoEn
        );
    }
}
