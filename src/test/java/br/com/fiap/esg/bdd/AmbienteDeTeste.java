package br.com.fiap.esg.bdd;

import br.com.fiap.esg.CidadesEsgApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Define qual API os cenarios vao exercitar.
 * Com a variavel BASE_URL: API ja em execucao (ex.: Docker Compose com PostgreSQL).
 * Sem ela: sobe a aplicacao real em porta aleatoria, com banco H2 em memoria.
 */
final class AmbienteDeTeste {

    private static ConfigurableApplicationContext aplicacao;
    private static String baseUrl;

    private AmbienteDeTeste() { }

    static synchronized void iniciar() {
        if (baseUrl != null) {
            return;
        }
        String externa = System.getenv("BASE_URL");
        if (externa != null && !externa.isBlank()) {
            baseUrl = externa.replaceAll("/+$", "");
            return;
        }
        aplicacao = new SpringApplicationBuilder(CidadesEsgApplication.class)
                .profiles("test")
                .run("--server.port=0",
                     "--spring.datasource.url=jdbc:h2:mem:esgbdd;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        baseUrl = "http://localhost:" + aplicacao.getEnvironment().getProperty("local.server.port");
    }

    static synchronized void encerrar() {
        if (aplicacao != null) {
            aplicacao.close();
            aplicacao = null;
        }
        baseUrl = null;
    }

    static String baseUrl() {
        return baseUrl;
    }
}
