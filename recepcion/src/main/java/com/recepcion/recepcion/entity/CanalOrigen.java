package com.recepcion.recepcion.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CanalOrigen {
    WHATSAPP("whatsapp"),
    TELEGRAM("telegram");

    private final String valor;

    CanalOrigen(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Deserializador personalizado que acepta tanto mayúsculas como minúsculas
     */
    @JsonCreator
    public static CanalOrigen fromString(String value) {
        if (value == null) {
            return null;
        }

        // Intentar coincidir por nombre del enum (WHATSAPP, TELEGRAM)
        for (CanalOrigen canal : CanalOrigen.values()) {
            if (canal.name().equalsIgnoreCase(value)) {
                return canal;
            }
        }

        // Intentar coincidir por valor (whatsapp, telegram)
        for (CanalOrigen canal : CanalOrigen.values()) {
            if (canal.getValor().equalsIgnoreCase(value)) {
                return canal;
            }
        }

        throw new IllegalArgumentException("Valor desconocido para CanalOrigen: " + value +
            ". Valores permitidos: WHATSAPP, TELEGRAM, whatsapp, telegram");
    }

    @Override
    public String toString() {
        return valor;
    }
}
