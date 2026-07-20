package com.puravida.modules.notifications.infrastructure.persistence;

import com.puravida.modules.notifications.domain.model.NotificationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationType attribute) {
        return attribute == null ? null : attribute.databaseValue();
    }

    @Override
    public NotificationType convertToEntityAttribute(String dbData) {
        return NotificationType.fromDatabaseValue(dbData);
    }
}
