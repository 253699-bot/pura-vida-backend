package com.puravida.modules.sales.application.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CreateManualSaleRequest {

    @NotNull(message = "Los items son obligatorios.")
    @Size(min = 1, message = "La venta debe contener al menos un item.")
    private final List<@NotNull(message = "Los items no pueden ser nulos.") @Valid CreateManualSaleItemRequest> items;

    private final String observaciones;

    @JsonIgnore
    private final Map<String, Object> unknownFields = new LinkedHashMap<>();

    @JsonCreator
    public CreateManualSaleRequest(
            @JsonProperty("items") List<CreateManualSaleItemRequest> items,
            @JsonProperty("observaciones") String observaciones
    ) {
        this.items = items;
        this.observaciones = observaciones;
    }

    @JsonProperty("items")
    public List<CreateManualSaleItemRequest> items() {
        return items;
    }

    @JsonProperty("observaciones")
    public String observaciones() {
        return observaciones;
    }

    @JsonAnySetter
    public void captureUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }

    @JsonIgnore
    @AssertTrue(message = "El body contiene campos no permitidos.")
    public boolean isWithoutUnknownFields() {
        return unknownFields.isEmpty();
    }

    @JsonIgnore
    public Set<String> unknownFieldNames() {
        return Set.copyOf(unknownFields.keySet());
    }
}
