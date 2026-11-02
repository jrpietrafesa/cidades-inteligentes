package br.com.fiap.esg.bdd;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

public class Ganchos {

    private final ContextoCenario contexto;

    public Ganchos(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @BeforeAll
    public static void subirAmbiente() {
        AmbienteDeTeste.iniciar();
    }

    @AfterAll
    public static void encerrarAmbiente() {
        AmbienteDeTeste.encerrar();
    }

    /** Em caso de falha, anexa ao relatorio a ultima resposta da API. */
    @After
    public void anexarRespostaEmCasoDeFalha(Scenario cenario) {
        if (cenario.isFailed() && contexto.resposta != null) {
            cenario.log("HTTP " + contexto.resposta.statusCode() + "\n" + contexto.resposta.asString());
        }
    }
}
