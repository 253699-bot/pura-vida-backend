package com.puravida.modules.sales.application.port.out;

import com.puravida.modules.sales.domain.model.ManualSaleMenuItem;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ManualSaleContextRepositoryPort {

    Optional<Boolean> lockBusinessOpenByDate(LocalDate date);

    List<ManualSaleMenuItem> lockMenuItemsByIds(List<Integer> menuItemIds);
}
