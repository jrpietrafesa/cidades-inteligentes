package br.com.fiap.esg.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Identifica o ambiente em execucao (staging/production) - usado nos smoke tests do pipeline. */
@RestController
public class InfoController {

    @Value("${app.environment:local}")
    private String ambiente;

    @Value("${app.version:dev}")
    private String versao;

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of(
                "aplicacao", "Cidades ESG Inteligentes",
                "ambiente", ambiente,
                "versao", versao);
    }
}
