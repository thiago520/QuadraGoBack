package com.quadrago.backend.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = false) // aplicado explicitamente via @Convert
public class DurationToMinutesConverter implements AttributeConverter<Duration, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Duration attribute) {
        if (attribute == null) return null;
        long minutes = attribute.toMinutes();
        if (minutes > Integer.MAX_VALUE) {
            // proteção simples; ajuste conforme sua regra de negócio
            throw new IllegalArgumentException("Duration too large to store in minutes: " + attribute);
        }
        return (int) minutes;
    }

    @Override
    public Duration convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return Duration.ofMinutes(dbData);
    }
}
