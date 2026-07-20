package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.orders.application.port.out.BusinessStatusForOrderRepositoryPort;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class CartSellabilityService {

    private final MenuForOrderRepositoryPort menuRepositoryPort;
    private final BusinessStatusForOrderRepositoryPort businessStatusRepositoryPort;

    public CartSellabilityService(
            MenuForOrderRepositoryPort menuRepositoryPort,
            BusinessStatusForOrderRepositoryPort businessStatusRepositoryPort
    ) {
        this.menuRepositoryPort = menuRepositoryPort;
        this.businessStatusRepositoryPort = businessStatusRepositoryPort;
    }

    public OrderableMenuItem requireSellableDish(Integer dishId) {
        LocalDate today = LocalDate.now();
        Boolean open = businessStatusRepositoryPort.findOpenByFecha(today)
                .orElseThrow(() -> new ConflictException(
                        "El estado de la fonda aun no ha sido configurado para hoy."
                ));
        if (!open) {
            throw new ConflictException("La fonda esta cerrada y no admite cambios al carrito.");
        }

        OrderableMenuItem menuItem = menuRepositoryPort.findByFecha(today).stream()
                .filter(item -> dishId.equals(item.dishId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("El platillo no pertenece al menu publicado de hoy."));
        if (!menuItem.dishActivo() || !menuItem.disponible()) {
            throw new ConflictException("El platillo no esta disponible.");
        }
        return menuItem;
    }
}
