package com.vr.miniautorizador.service;

import com.vr.miniautorizador.domain.Cartao;
import com.vr.miniautorizador.domain.TransacaoStatus;
import com.vr.miniautorizador.dto.TransacaoDto;
import com.vr.miniautorizador.repository.CartaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransacaoService {

    private final CartaoRepository cartaoRepository;

    public TransacaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    @Transactional
    public TransacaoStatus processarTransacao(TransacaoDto dto) {
        return cartaoRepository.findByNumeroCartaoForUpdate(dto.numeroCartao())
                .map(cartao -> validarSenha(cartao, dto))
                .orElse(TransacaoStatus.CARTAO_INEXISTENTE);
    }

    private TransacaoStatus validarSenha(Cartao cartao, TransacaoDto dto) {
        return Optional.of(cartao)
                .filter(c -> c.getSenha().equals(dto.senhaCartao()))
                .map(c -> validarSaldoEDebitar(c, dto.valor()))
                .orElse(TransacaoStatus.SENHA_INVALIDA);
    }

    private TransacaoStatus validarSaldoEDebitar(Cartao cartao, BigDecimal valor) {
        return Optional.of(cartao)
                .filter(c -> c.getSaldo().compareTo(valor) >= 0)
                .map(c -> efetuarDebito(c, valor))
                .orElse(TransacaoStatus.SALDO_INSUFICIENTE);
    }

    private TransacaoStatus efetuarDebito(Cartao cartao, BigDecimal valor) {
        cartao.setSaldo(cartao.getSaldo().subtract(valor));
        cartaoRepository.save(cartao);
        return TransacaoStatus.OK;
    }
}
