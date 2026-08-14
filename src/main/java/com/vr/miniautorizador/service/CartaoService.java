package com.vr.miniautorizador.service;

import com.vr.miniautorizador.domain.Cartao;
import com.vr.miniautorizador.dto.CartaoResponseDto;
import com.vr.miniautorizador.dto.CriarCartaoDto;
import com.vr.miniautorizador.repository.CartaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartaoService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    private final CartaoRepository cartaoRepository;

    public CartaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    @Transactional
    public Optional<CartaoResponseDto> criarCartao(CriarCartaoDto dto) {
        return cartaoRepository.findByNumeroCartao(dto.numeroCartao())
            .map(existing -> Optional.<CartaoResponseDto>empty())
            .orElseGet(() -> Optional.of(salvarNovoCartao(dto)));
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> obterSaldo(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao)
            .map(Cartao::getSaldo);
    }

    private CartaoResponseDto salvarNovoCartao(CriarCartaoDto dto) {
        Cartao novoCartao = new Cartao(dto.numeroCartao(), dto.senha(), INITIAL_BALANCE);
        Cartao salvo = cartaoRepository.save(novoCartao);
        return new CartaoResponseDto(salvo.getNumeroCartao(), salvo.getSenha());
    }
}
