package com.puravida.modules.menu.application.port.out;

import com.puravida.modules.menu.application.dto.DishImageContent;
import com.puravida.modules.menu.application.dto.StoredDishImage;

public interface DishImageStoragePort {
    StoredDishImage store(byte[] content, String declaredMediaType);
    DishImageContent load(String key);
    void delete(String key);
}