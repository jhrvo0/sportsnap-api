package com.sportsnap.gamification.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "atributos_esportivos")
public class AtributoEsportivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private Double peso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carta_oficial_id", nullable = false)
    private CartaOficial cartaOficial;

    public AtributoEsportivo() {}

    public AtributoEsportivo(String nome, Double valor, Double peso, CartaOficial cartaOficial) {
        this.nome = nome;
        this.valor = valor;
        this.peso = peso;
        this.cartaOficial = cartaOficial;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public CartaOficial getCartaOficial() { return cartaOficial; }
    public void setCartaOficial(CartaOficial cartaOficial) { this.cartaOficial = cartaOficial; }
}
