package com.vr.miniautorizador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.dto.CriarCartaoDto;
import com.vr.miniautorizador.dto.TransacaoDto;
import com.vr.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Test
    @DisplayName("Garantir que 2 criações simultâneas do mesmo cartão não causam race condition e exatamente 1 é criada (422 na outra)")
    void deveGarantirConcorrenciaNaCriacaoDeCartaoSimultanea() throws Exception {
        String numeroCartao = "6549873025634599";
        String senha = "1234";

        CriarCartaoDto criarDto = new CriarCartaoDto(numeroCartao, senha);
        String jsonPayload = objectMapper.writeValueAsString(criarDto);

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger criadosComSucesso = new AtomicInteger(0);
        AtomicInteger duplicadosRejeitados = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    MvcResult result = mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                        .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 201) {
                        criadosComSucesso.incrementAndGet();
                    } else if (status == 422) {
                        duplicadosRejeitados.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(1, criadosComSucesso.get(), "Exatamente 1 cartão deve ser criado com sucesso (201)");
        assertEquals(1, duplicadosRejeitados.get(), "Exatamente 1 criação concorrente deve ser rejeitada (422)");
    }

    @Test
    @DisplayName("Garantir que 2 transações simultâneas de R$10 numa conta com R$10 de saldo não causam problema de concorrência")
    void deveGarantirConcorrenciaEmTransacoesSimultaneas() throws Exception {
        String numeroCartao = "6549873025634506";
        String senha = "1234";

        // 1. Criar cartão com saldo R$ 500.00
        CriarCartaoDto criarDto = new CriarCartaoDto(numeroCartao, senha);
        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarDto)))
            .andExpect(status().isCreated());

        // 2. Debitar R$ 490.00 deixando exatamente R$ 10.00 de saldo
        TransacaoDto debitoInicial = new TransacaoDto(numeroCartao, senha, new BigDecimal("490.00"));
        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitoInicial)))
            .andExpect(status().isCreated())
            .andExpect(content().string("OK"));

        // Confirmar saldo R$ 10.00
        mockMvc.perform(get("/cartoes/" + numeroCartao))
            .andExpect(status().isOk())
            .andExpect(content().string("10.00"));

        // 3. Disparar 2 transações simultâneas de R$ 10.00 em threads paralelas
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger saldoInsuficiente = new AtomicInteger(0);

        TransacaoDto transacaoSimultanea = new TransacaoDto(numeroCartao, senha, new BigDecimal("10.00"));
        String jsonPayload = objectMapper.writeValueAsString(transacaoSimultanea);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // aguarda o sinal para disparar simultaneamente
                    MvcResult result = mockMvc.perform(post("/transacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                        .andReturn();

                    int status = result.getResponse().getStatus();
                    String responseBody = result.getResponse().getContentAsString();

                    if (status == 201 && "OK".equals(responseBody)) {
                        sucessos.incrementAndGet();
                    } else if (status == 422 && "SALDO_INSUFICIENTE".equals(responseBody)) {
                        saldoInsuficiente.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // dispara todas as threads ao mesmo tempo
        doneLatch.await();
        executor.shutdown();

        // 4. Validações de Concorrência
        assertEquals(1, sucessos.get(), "Exatamente 1 transação deve ser aprovada");
        assertEquals(1, saldoInsuficiente.get(), "Exatamente 1 transação deve falhar por saldo insuficiente");

        // Saldo final deve ser 0.00
        mockMvc.perform(get("/cartoes/" + numeroCartao))
            .andExpect(status().isOk())
            .andExpect(content().string("0.00"));
    }
}
