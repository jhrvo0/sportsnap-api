package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public enum Intensidade {

    BAIXA(1),
    MEDIA(2),
    ALTA(3);

    private final int multiplicador;

    Intensidade(int multiplicador) {
        this.multiplicador = multiplicador;
    }

    public int getMultiplicador() {
        return multiplicador;
    }

    public static Intensidade apartirDeTexto(String texto) {
        notNull(texto, "A intensidade nao pode ser nula");
        notBlank(texto, "A intensidade nao pode estar em branco");
        return switch (texto.trim().toLowerCase()) {
            case "alta" -> ALTA;
            case "media", "média" -> MEDIA;
            case "baixa" -> BAIXA;
            default -> throw new IllegalArgumentException("Intensidade invalida: " + texto);
        };
    }
}
