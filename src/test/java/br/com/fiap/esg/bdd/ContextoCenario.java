package br.com.fiap.esg.bdd;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Estado de um cenario, compartilhado entre as classes de passos. */
public class ContextoCenario {

    Response resposta;
    Long edificioId;
    Long alertaId;
    final Map<String, BigDecimal> indicadoresIniciais = new HashMap<>();

    RequestSpecification api() {
        return RestAssured.given().baseUri(AmbienteDeTeste.baseUrl()).contentType(ContentType.JSON);
    }

    Object campo(String caminho) {
        return resposta.jsonPath().get(caminho);
    }

    BigDecimal numero(String caminho) {
        Object valor = campo(caminho);
        if (valor == null) {
            throw new AssertionError("Campo ausente na resposta: " + caminho);
        }
        return new BigDecimal(valor.toString());
    }
}
