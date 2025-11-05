package com.recepcion.recepcion.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter para CanalOrigen que guarda los valores en minúsculas en la BD
 */
@Converter(autoApply = true)
public class CanalOrigenConverter implements AttributeConverter<CanalOrigen, String> {

    @Override
    public String convertToDatabaseColumn(CanalOrigen canalOrigen) {
        if (canalOrigen == null) {
            return null;
        }
        return canalOrigen.getValor();
    }

    @Override
    public CanalOrigen convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        // Buscar el enum que coincida con el valor de la BD
        for (CanalOrigen canal : CanalOrigen.values()) {
            if (canal.getValor().equals(dbData)) {
                return canal;
            }
        }

        throw new IllegalArgumentException("Valor desconocido para CanalOrigen: " + dbData);
    }
}
