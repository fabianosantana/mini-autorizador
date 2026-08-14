package com.vr.miniautorizador.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "cartoes", indexes = {
        @Index(name = "idx_cartao_numero", columnList = "numero_cartao", unique = true)
})
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cartao", nullable = false, unique = true, length = 32)
    private String numeroCartao;

    @Column(name = "senha", nullable = false, length = 128)
    private String senha;

    @Column(name = "saldo", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    /**
     * Construtor padrão exigido por frameworks como JPA/Hibernate.
     * Visibilidade protected evita instanciação de objetos vazios pela aplicação.
     */
    protected Cartao() {
    }

    public Cartao(String numeroCartao, String senha, BigDecimal saldo) {
        this.numeroCartao = numeroCartao;
        this.senha = senha;
        this.saldo = saldo;
    }

    // --- Comportamentos de Negócio (Rich Domain) ---

    public boolean senhaConfere(String senhaFornecida) {
        return Optional.ofNullable(senhaFornecida)
            .map(this.senha::equals)
            .orElse(false);
    }

    public boolean possuiSaldoPara(BigDecimal valorDebito) {
        return Optional.ofNullable(valorDebito)
            .map(v -> this.saldo.compareTo(v) >= 0)
            .orElse(false);
    }

    public void debitar(BigDecimal valorDebito) {
        this.saldo = this.saldo.subtract(valorDebito);
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}