package com.vr.miniautorizador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransacaoDto(
    @NotBlank(message = "Número do cartão é obrigatório")
    String numeroCartao,

    @NotBlank(message = "Senha do cartão é obrigatória")
    String senhaCartao,

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser maior que zero")
    BigDecimal valor
) {
}
