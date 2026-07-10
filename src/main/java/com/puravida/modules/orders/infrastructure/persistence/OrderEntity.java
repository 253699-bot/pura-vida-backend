package com.puravida.modules.orders.infrastructure.persistence;

import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "PEDIDOS")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_pedido")
    private Integer id;

    @Column(name = "Id_cliente")
    private Integer clienteId;

    @Column(name = "Fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "Hora", nullable = false)
    private LocalTime hora;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "Estado", nullable = false, columnDefinition = "ENUM('pendiente','aceptado','rechazado','cancelado')")
    private OrderStatus estado;

    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "Tiempo_espera_est", length = 100)
    private String tiempoEsperaEstimado;

    @Column(name = "Motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "Respondido_por")
    private Integer respondidoPor;

    @Column(name = "Respondido_en")
    private LocalDateTime respondidoEn;

    @Column(name = "Observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected OrderEntity() {
    }

    private OrderEntity(
            Integer id,
            Integer clienteId,
            LocalDate fecha,
            LocalTime hora,
            OrderStatus estado,
            BigDecimal total,
            String tiempoEsperaEstimado,
            String motivoRechazo,
            Integer respondidoPor,
            LocalDateTime respondidoEn,
            String observaciones,
            LocalDateTime creadoEn
    ) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.total = total;
        this.tiempoEsperaEstimado = tiempoEsperaEstimado;
        this.motivoRechazo = motivoRechazo;
        this.respondidoPor = respondidoPor;
        this.respondidoEn = respondidoEn;
        this.observaciones = observaciones;
        this.creadoEn = creadoEn;
    }

    public static OrderEntity fromDomain(Order order) {
        return new OrderEntity(
                order.id(),
                order.clienteId(),
                order.fecha(),
                order.hora(),
                order.estado(),
                order.total(),
                order.tiempoEsperaEstimado(),
                order.motivoRechazo(),
                order.respondidoPor(),
                order.respondidoEn(),
                order.observaciones(),
                order.creadoEn()
        );
    }

    public Order toDomain() {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                estado,
                total,
                tiempoEsperaEstimado,
                motivoRechazo,
                respondidoPor,
                respondidoEn,
                observaciones,
                creadoEn
        );
    }
}
