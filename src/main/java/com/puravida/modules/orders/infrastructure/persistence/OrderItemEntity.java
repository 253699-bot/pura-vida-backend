package com.puravida.modules.orders.infrastructure.persistence;

import com.puravida.modules.orders.domain.model.OrderItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_PEDIDO")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_detalle_pedido")
    private Integer id;

    @Column(name = "Id_pedido", nullable = false)
    private Integer orderId;

    @Column(name = "Id_platillo")
    private Integer dishId;

    @Column(name = "Id_menu")
    private Integer menuItemId;

    @Column(name = "Nombre_platillo", nullable = false, length = 150)
    private String nombrePlatillo;

    @Column(name = "Cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "Precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "Subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    protected OrderItemEntity() {
    }

    private OrderItemEntity(
            Integer id,
            Integer orderId,
            Integer dishId,
            Integer menuItemId,
            String nombrePlatillo,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.menuItemId = menuItemId;
        this.nombrePlatillo = nombrePlatillo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public static OrderItemEntity fromDomain(OrderItem item) {
        return new OrderItemEntity(
                item.id(),
                item.orderId(),
                item.dishId(),
                item.menuItemId(),
                item.nombrePlatillo(),
                item.cantidad(),
                item.precioUnitario(),
                item.subtotal()
        );
    }

    public OrderItem toDomain() {
        return new OrderItem(
                id,
                orderId,
                dishId,
                menuItemId,
                nombrePlatillo,
                cantidad,
                precioUnitario,
                subtotal
        );
    }
}
