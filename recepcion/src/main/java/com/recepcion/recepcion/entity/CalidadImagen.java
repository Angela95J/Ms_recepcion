package com.recepcion.recepcion.entity;

public enum CalidadImagen {
    EXCELENTE("excelente"),
    BUENA("buena"),
    REGULAR("regular"),
    MALA("mala");

    private final String valor;

    CalidadImagen(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
