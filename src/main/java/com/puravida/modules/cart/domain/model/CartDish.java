package com.puravida.modules.cart.domain.model;

import java.math.BigDecimal;

public record CartDish(Integer id, String nombre, BigDecimal precioBase, boolean activo) {
}
