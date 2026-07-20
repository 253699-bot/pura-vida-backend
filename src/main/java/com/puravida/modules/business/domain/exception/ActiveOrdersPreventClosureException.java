package com.puravida.modules.business.domain.exception;

import com.puravida.modules.business.domain.model.ActiveBusinessOrderCounts;
import com.puravida.shared.domain.exception.ConflictException;

public class ActiveOrdersPreventClosureException extends ConflictException {

    private final ActiveBusinessOrderCounts counts;

    public ActiveOrdersPreventClosureException(ActiveBusinessOrderCounts counts) {
        super("No puedes cerrar la fonda mientras existan pedidos pendientes o aceptados.");
        this.counts = counts;
    }

    public ActiveBusinessOrderCounts counts() {
        return counts;
    }
}