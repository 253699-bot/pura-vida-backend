package com.puravida.modules.notifications.infrastructure.persistence;

import com.puravida.modules.notifications.domain.model.Notification;
import com.puravida.modules.notifications.domain.model.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACIONES")
public class NotificationEntity {

    private static final String UNREAD = "no_leida";
    private static final String READ = "leida";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_notificacion")
    private Integer id;

    @Column(name = "Id_usuario", nullable = false)
    private Integer usuarioId;

    @Column(name = "Id_pedido")
    private Integer pedidoId;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(name = "Tipo", nullable = false, length = 50)
    private NotificationType tipo;

    @Column(name = "Titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "Mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "Estado", nullable = false, columnDefinition = "ENUM('no_leida','leida')")
    private String estado;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "Leida_en")
    private LocalDateTime leidaEn;

    protected NotificationEntity() {
    }

    private NotificationEntity(
            Integer id,
            Integer usuarioId,
            Integer pedidoId,
            NotificationType tipo,
            String titulo,
            String mensaje,
            String estado,
            LocalDateTime creadoEn,
            LocalDateTime leidaEn
    ) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.estado = estado;
        this.creadoEn = creadaEnOrNow(creadoEn);
        this.leidaEn = leidaEn;
    }

    public static NotificationEntity fromDomain(Notification notification) {
        return new NotificationEntity(
                notification.id(),
                notification.usuarioId(),
                notification.pedidoId(),
                notification.tipo(),
                notification.titulo(),
                notification.mensaje(),
                notification.leida() ? READ : UNREAD,
                notification.creadaEn(),
                notification.leidaEn()
        );
    }

    public Notification toDomain() {
        return new Notification(
                id,
                usuarioId,
                pedidoId,
                tipo,
                titulo,
                mensaje,
                READ.equals(estado),
                creadoEn,
                leidaEn
        );
    }

    private static LocalDateTime creadaEnOrNow(LocalDateTime creadaEn) {
        return creadaEn == null ? LocalDateTime.now() : creadaEn;
    }
}
