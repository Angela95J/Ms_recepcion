package com.recepcion.recepcion.entity;

public enum TipoArchivo {
    IMAGEN("imagen"),
    AUDIO("audio"),
    VIDEO("video");

    private final String valor;

    TipoArchivo(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
