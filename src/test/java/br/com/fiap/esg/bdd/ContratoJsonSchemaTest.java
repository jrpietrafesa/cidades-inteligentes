package br.com.fiap.esg.bdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/** Garante que os contratos reprovam respostas erradas, e nao apenas aprovam as certas. */
class ContratoJsonSchemaTest {

    private static final String EDIFICIO = """
            {"id":1,"nome":"Escola","cidade":"Sao Paulo","tipo":"ESCOLA","limiteDiarioKwh":100.00}
            """;

    @Test
    @DisplayName("Contrato aprova resposta correta")
    void aprovaRespostaCorreta() {
        assertThat(EDIFICIO, matchesJsonSchemaInClasspath("schemas/edificio.json"));
    }

    @Test
    @DisplayName("Contrato reprova campo obrigatorio ausente")
    void reprovaCampoAusente() {
        assertThat("{\"id\":1,\"nome\":\"Escola\"}", not(matchesJsonSchemaInClasspath("schemas/edificio.json")));
    }

    @Test
    @DisplayName("Contrato reprova tipo incorreto")
    void reprovaTipoIncorreto() {
        assertThat(EDIFICIO.replace("100.00", "\"cem\""), not(matchesJsonSchemaInClasspath("schemas/edificio.json")));
    }

    @Test
    @DisplayName("Contrato reprova campo nao previsto")
    void reprovaCampoExtra() {
        assertThat(EDIFICIO.replace("}", ",\"senha\":\"x\"}"), not(matchesJsonSchemaInClasspath("schemas/edificio.json")));
    }

    @Test
    @DisplayName("Contrato de lista reprova item invalido")
    void reprovaItemDeLista() {
        assertThat("[{\"id\":1}]", not(matchesJsonSchemaInClasspath("schemas/edificio-lista.json")));
    }
}
