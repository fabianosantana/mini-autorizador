package com.vr.miniautorizador.service;

import com.vr.miniautorizador.domain.Cartao;
import com.vr.miniautorizador.domain.TransacaoStatus;
import com.vr.miniautorizador.dto.TransacaoDto;
import com.vr.miniautorizador.repository.CartaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransacaoService {

    private final CartaoRepository cartaoRepository;

    public TransacaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    @Transactional
    public TransacaoStatus processarTransacao(TransacaoDto dto) {
        return cartaoRepository.findByNumeroCartaoForUpdate(dto.numeroCartao())
            .map(cartao -> validarEProcessar(cartao, dto))
            .orElse(TransacaoStatus.CARTAO_INEXISTENTE);
    }

    private TransacaoStatus validarEProcessar(Cartao cartao, TransacaoDto dto) {
        return switch (cartao) {
            case Cartao c when !c.senhaConfere(dto.senhaCartao()) -> TransacaoStatus.SENHA_INVALIDA;
            case Cartao c when !c.possuiSaldoPara(dto.valor()) -> TransacaoStatus.SALDO_INSUFICIENTE;
            case Cartao c -> efetuarDebito(c, dto.valor());
        };
    }

    private TransacaoStatus efetuarDebito(Cartao cartao, BigDecimal valor) {
        cartao.debitar(valor);
        cartaoRepository.save(cartao);
        return TransacaoStatus.OK;
    }
}
