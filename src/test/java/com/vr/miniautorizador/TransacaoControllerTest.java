package com.vr.miniautorizador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.dto.CriarCartaoDto;
import com.vr.miniautorizador.dto.TransacaoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve autorizar transação com sucesso (201 OK) e atualizar saldo")
    void deveAutorizarTransacaoComSucesso() throws Exception {
        CriarCartaoDto criarDto = new CriarCartaoDto("6549873025634503", "1234");
        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDto)))
            .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("6549873025634503", "1234", new BigDecimal("10.00"));
        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
            .andExpect(status().isCreated())
            .andExpect(content().string("OK"));

        mockMvc.perform(get("/cartoes/6549873025634503"))
            .andExpect(status().isOk())
            .andExpect(content().string("490.00"));
    }

    @Test
    @DisplayName("Deve barrar transação por cartão inexistente (422 CARTAO_INEXISTENTE)")
    void deveBarrarTransacaoCartaoInexistente() throws Exception {
        TransacaoDto transacaoDto = new TransacaoDto("9999999999999999", "1234", new BigDecimal("10.00"));

        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    @DisplayName("Deve barrar transação por senha inválida (422 SENHA_INVALIDA)")
    void deveBarrarTransacaoSenhaInvalida() throws Exception {
        CriarCartaoDto criarDto = new CriarCartaoDto("6549873025634504", "1234");
        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDto)))
            .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("6549873025634504", "0000", new BigDecimal("10.00"));
        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("SENHA_INVALIDA"));
    }

    @Test
    @DisplayName("Deve barrar transação por saldo insuficiente (422 SALDO_INSUFICIENTE)")
    void deveBarrarTransacaoSaldoInsuficiente() throws Exception {
        CriarCartaoDto criarDto = new CriarCartaoDto("6549873025634505", "1234");
        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDto)))
            .andExpect(status().isCreated());

        TransacaoDto transacaoDto = new TransacaoDto("6549873025634505", "1234", new BigDecimal("500.01"));
        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transacaoDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("SALDO_INSUFICIENTE"));
    }
}
