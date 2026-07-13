package com.puravida.modules.cart.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartItemResponse> items, BigDecimal total) {
}
