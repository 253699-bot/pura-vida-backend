package com.puravida.modules.orders.application.port.out;

import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import java.time.LocalDate;
import java.util.List;

public interface MenuForOrderRepositoryPort {

    List<OrderableMenuItem> findByFecha(LocalDate fecha);
}
