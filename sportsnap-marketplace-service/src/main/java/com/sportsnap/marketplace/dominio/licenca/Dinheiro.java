package com.sportsnap.marketplace.dominio.licenca;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Dinheiro {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    private final BigDecimal valor;

    public Dinheiro(BigDecimal valor) {
        notNull(valor, "O valor monetario nao pode ser nulo");
        isTrue(valor.signum() >= 0, "O valor monetario nao pode ser negativo");
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        return new Dinheiro(valor.multiply(fator).setScale(2, RoundingMode.HALF_UP));
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dinheiro outro && valor.compareTo(outro.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "R$ " + valor;
    }
}
