package com.vr.miniautorizador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.dto.CriarCartaoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve criar cartão com sucesso e retornar status 201")
    void deveCriarCartaoComSucesso() throws Exception {
        CriarCartaoDto dto = new CriarCartaoDto("6549873025634501", "1234");

        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
            .andExpect(jsonPath("$.senha").value("1234"));
    }

    @Test
    @DisplayName("Deve retornar status 422 ao tentar criar cartão com número já existente")
    void deveRetornar422AoCriarCartaoDuplicado() throws Exception {
        CriarCartaoDto dto = new CriarCartaoDto("6549873025634501", "1234");

        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
            .andExpect(jsonPath("$.senha").value("1234"));
    }

    @Test
    @DisplayName("Deve obter saldo do cartão recém-criado com sucesso (200)")
    void deveObterSaldoCartaoExistente() throws Exception {
        CriarCartaoDto dto = new CriarCartaoDto("6549873025634502", "1234");

        mockMvc.perform(post("/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/cartoes/6549873025634502"))
            .andExpect(status().isOk())
            .andExpect(content().string("500.00"));
    }

    @Test
    @DisplayName("Deve retornar 404 sem corpo ao consultar saldo de cartão inexistente")
    void deveRetornar404AoObterSaldoCartaoInexistente() throws Exception {
        mockMvc.perform(get("/cartoes/9999999999999999"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }
}
