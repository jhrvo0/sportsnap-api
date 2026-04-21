package com.sportsnap.gamification.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "atletas")
public class Atleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @OneToOne(mappedBy = "atleta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CartaOficial cartaOficial;

    @OneToOne(mappedBy = "atleta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StatusPotencial statusPotencial;

    public Atleta() {}

    public Atleta(String nome, String email) {
        this.nome = nome;
        this.email = email;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public CartaOficial getCartaOficial() { return cartaOficial; }
    public void setCartaOficial(CartaOficial cartaOficial) { this.cartaOficial = cartaOficial; }

    public StatusPotencial getStatusPotencial() { return statusPotencial; }
    public void setStatusPotencial(StatusPotencial statusPotencial) { this.statusPotencial = statusPotencial; }
}
