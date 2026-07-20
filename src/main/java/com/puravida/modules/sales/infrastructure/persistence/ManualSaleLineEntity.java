package com.puravida.modules.sales.infrastructure.persistence;

import com.puravida.modules.sales.domain.model.ManualSaleLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_PEDIDO")
public class ManualSaleLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_detalle_pedido")
    private Integer id;

    @Column(name = "Id_pedido", insertable = false, updatable = false)
    private Integer orderId;

    @Column(name = "Id_venta", nullable = false)
    private Integer saleId;

    @Column(name = "Id_platillo")
    private Integer dishId;

    @Column(name = "Id_menu", nullable = false)
    private Integer menuItemId;

    @Column(name = "Nombre_platillo", nullable = false, length = 150)
    private String dishName;

    @Column(name = "Cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "Precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    protected ManualSaleLineEntity() {
    }

    private ManualSaleLineEntity(
            Integer id,
            Integer saleId,
            Integer dishId,
            Integer menuItemId,
            String dishName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        this.id = id;
        this.saleId = saleId;
        this.dishId = dishId;
        this.menuItemId = menuItemId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public static ManualSaleLineEntity fromDomain(ManualSaleLine line) {
        return new ManualSaleLineEntity(
                line.id(),
                line.saleId(),
                line.dishId(),
                line.menuItemId(),
                line.dishName(),
                line.quantity(),
                line.unitPrice(),
                line.subtotal()
        );
    }

    public ManualSaleLine toDomain() {
        return new ManualSaleLine(
                id,
                saleId,
                dishId,
                menuItemId,
                dishName,
                quantity,
                unitPrice,
                subtotal
        );
    }
}
