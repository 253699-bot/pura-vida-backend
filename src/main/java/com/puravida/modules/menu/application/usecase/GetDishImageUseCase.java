package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.menu.application.dto.DishImageContent;
import com.puravida.modules.menu.application.port.in.GetDishImagePort;
import com.puravida.modules.menu.application.port.out.DishImageStoragePort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetDishImageUseCase implements GetDishImagePort {

    private final DishRepositoryPort dishRepositoryPort;
    private final DishImageStoragePort storagePort;

    public GetDishImageUseCase(
            DishRepositoryPort dishRepositoryPort,
            DishImageStoragePort storagePort
    ) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.storagePort = storagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public DishImageContent get(Integer dishId) {
        Dish dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        if (dish.imagenKey() == null || dish.imagenKey().isBlank()) {
            throw new NotFoundException("La imagen del platillo no esta disponible.");
        }
        return storagePort.load(dish.imagenKey());
    }
}