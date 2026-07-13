package com.puravida.modules.sales.infrastructure.persistence;

import com.puravida.modules.sales.domain.model.SaleSource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SaleSourceConverter implements AttributeConverter<SaleSource, String> {

    @Override
    public String convertToDatabaseColumn(SaleSource attribute) {
        return attribute == null ? null : attribute.databaseValue();
    }

    @Override
    public SaleSource convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SaleSource.fromDatabaseValue(dbData);
    }
}
