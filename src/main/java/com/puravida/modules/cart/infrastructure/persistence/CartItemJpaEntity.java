package com.puravida.modules.cart.infrastructure.persistence;

import com.puravida.modules.cart.domain.model.CartItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CARRITO_ITEMS")
public class CartItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_carrito_item")
    private Integer id;

    @Column(name = "Id_usuario", nullable = false)
    private Integer userId;

    @Column(name = "Id_platillo", nullable = false)
    private Integer dishId;

    @Column(name = "Cantidad", nullable = false)
    private int cantidad;

    @Column(name = "Precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "Creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "Actualizado_en")
    private LocalDateTime actualizadoEn;

    protected CartItemJpaEntity() {
    }

    private CartItemJpaEntity(
            Integer id,
            Integer userId,
            Integer dishId,
            int cantidad,
            BigDecimal precioUnitario,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        this.id = id;
        this.userId = userId;
        this.dishId = dishId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static CartItemJpaEntity fromDomain(CartItem item) {
        return new CartItemJpaEntity(
                item.id(),
                item.userId(),
                item.dishId(),
                item.cantidad(),
                item.precioUnitario(),
                item.creadoEn(),
                item.actualizadoEn()
        );
    }

    public CartItem toDomain() {
        return new CartItem(id, userId, dishId, cantidad, precioUnitario, creadoEn, actualizadoEn);
    }
}
