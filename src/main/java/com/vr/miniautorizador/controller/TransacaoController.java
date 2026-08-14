package com.vr.miniautorizador.controller;

import com.vr.miniautorizador.dto.TransacaoDto;
import com.vr.miniautorizador.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<String> autorizarTransacao(@Valid @RequestBody TransacaoDto dto) {
        return transacaoService.processarTransacao(dto).toResponseEntity();
    }
}
