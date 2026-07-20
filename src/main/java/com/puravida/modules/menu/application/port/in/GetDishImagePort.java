package com.puravida.modules.menu.application.port.in;

import com.puravida.modules.menu.application.dto.DishImageContent;

public interface GetDishImagePort {
    DishImageContent get(Integer dishId);
}