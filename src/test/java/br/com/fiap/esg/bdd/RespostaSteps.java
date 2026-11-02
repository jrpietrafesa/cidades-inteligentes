package br.com.fiap.esg.bdd;

import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;

import java.math.BigDecimal;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

/** Validacoes comuns: status code, corpo JSON e contrato. */
public class RespostaSteps {

    private final ContextoCenario contexto;

    public RespostaSteps(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Entao("o status da resposta deve ser {int}")
    public void statusDeveSer(int esperado) {
        assertThat(contexto.resposta.statusCode())
                .as("corpo: %s", contexto.resposta.asString())
                .isEqualTo(esperado);
    }

    @E("a resposta deve respeitar o contrato {string}")
    public void respeitaContrato(String schema) {
        assertThat(contexto.resposta.asString(), matchesJsonSchemaInClasspath("schemas/" + schema));
    }

    @E("o campo {string} deve ser {string}")
    public void campoTexto(String campo, String esperado) {
        assertThat(String.valueOf(contexto.campo(campo))).isEqualTo(esperado);
    }

    @E("o campo {string} deve valer {kwh}")
    public void campoNumerico(String campo, BigDecimal esperado) {
        assertThat(contexto.numero(campo)).isEqualByComparingTo(esperado);
    }

    @E("o campo {string} deve estar preenchido")
    public void campoPreenchido(String campo) {
        assertThat(contexto.campo(campo)).isNotNull();
    }

    @E("a mensagem de erro deve citar {string}")
    public void mensagemDeErro(String trecho) {
        assertThat(contexto.resposta.jsonPath().getString("detail")).contains(trecho);
    }
}
