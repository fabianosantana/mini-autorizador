package com.vr.miniautorizador.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public enum TransacaoStatus {
    OK(HttpStatus.CREATED, "OK"),
    CARTAO_INEXISTENTE(HttpStatus.UNPROCESSABLE_ENTITY, "CARTAO_INEXISTENTE"),
    SENHA_INVALIDA(HttpStatus.UNPROCESSABLE_ENTITY, "SENHA_INVALIDA"),
    SALDO_INSUFICIENTE(HttpStatus.UNPROCESSABLE_ENTITY, "SALDO_INSUFICIENTE");

    private final HttpStatus httpStatus;
    private final String responseBody;

    TransacaoStatus(HttpStatus httpStatus, String responseBody) {
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public ResponseEntity<String> toResponseEntity() {
        return ResponseEntity.status(httpStatus).body(responseBody);
    }
}
