package com.puravida.modules.sales.infrastructure.persistence;

import com.puravida.modules.sales.domain.model.SaleStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SaleStatusConverter implements AttributeConverter<SaleStatus, String> {

    @Override
    public String convertToDatabaseColumn(SaleStatus attribute) {
        return attribute == null ? null : attribute.databaseValue();
    }

    @Override
    public SaleStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SaleStatus.fromDatabaseValue(dbData);
    }
}
