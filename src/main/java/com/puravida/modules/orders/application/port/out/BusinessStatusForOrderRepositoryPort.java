package com.puravida.modules.orders.application.port.out;

import java.time.LocalDate;
import java.util.Optional;

public interface BusinessStatusForOrderRepositoryPort {

    Optional<Boolean> findOpenByFecha(LocalDate fecha);
}
