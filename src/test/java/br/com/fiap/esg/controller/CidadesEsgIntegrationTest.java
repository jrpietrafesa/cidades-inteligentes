package br.com.fiap.esg.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Teste de integracao ponta a ponta da API (Spring + JPA + H2). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CidadesEsgIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void fluxoCompletoDeMonitoramento() throws Exception {
        MvcResult criado = mvc.perform(post("/api/edificios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Hospital Municipal Norte","cidade":"Sao Paulo",
                                 "tipo":"HOSPITAL","limiteDiarioKwh":500}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        Integer id = JsonPath.read(criado.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/api/edificios/" + id + "/medicoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kwh\":300,\"dataHora\":\"2026-10-01T08:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertaGerado").value(false));

        mvc.perform(post("/api/edificios/" + id + "/medicoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kwh\":250,\"dataHora\":\"2026-10-01T18:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertaGerado").value(true));

        mvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].edificio", hasItem("Hospital Municipal Norte")));

        mvc.perform(get("/api/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasAbertos", greaterThanOrEqualTo(1)));
    }

    @Test
    void validacaoRejeitaLimiteInvalido() throws Exception {
        mvc.perform(post("/api/edificios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"cidade\":\"Y\",\"tipo\":\"Z\",\"limiteDiarioKwh\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void edificioInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/edificios/9999")).andExpect(status().isNotFound());
    }

    @Test
    void infoEHealthDisponiveis() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente").value("test"));
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
