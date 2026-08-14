package com.vr.miniautorizador.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarCartaoDto(
    @NotBlank(message = "Número do cartão é obrigatório")
    String numeroCartao,

    @NotBlank(message = "Senha é obrigatória")
    String senha
) {
}
