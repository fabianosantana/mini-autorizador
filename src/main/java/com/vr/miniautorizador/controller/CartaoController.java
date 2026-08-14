package com.vr.miniautorizador.controller;

import com.vr.miniautorizador.dto.CartaoResponseDto;
import com.vr.miniautorizador.dto.CriarCartaoDto;
import com.vr.miniautorizador.service.CartaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cartoes")
public class CartaoController {

    private final CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @PostMapping
    public ResponseEntity<CartaoResponseDto> criarCartao(@Valid @RequestBody CriarCartaoDto dto) {
        return cartaoService.criarCartao(dto)
            .map(cartao -> ResponseEntity.status(HttpStatus.CREATED).body(cartao))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new CartaoResponseDto(dto.numeroCartao(), dto.senha())));
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> obterSaldo(@PathVariable String numeroCartao) {
        return cartaoService.obterSaldo(numeroCartao)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
