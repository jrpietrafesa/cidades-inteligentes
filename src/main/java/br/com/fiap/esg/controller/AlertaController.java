package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.AlertaResponse;
import br.com.fiap.esg.dto.IndicadoresResponse;
import br.com.fiap.esg.service.MonitoramentoEnergeticoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AlertaController {

    private final MonitoramentoEnergeticoService service;

    public AlertaController(MonitoramentoEnergeticoService service) {
        this.service = service;
    }

    @GetMapping("/alertas")
    public List<AlertaResponse> alertasAbertos() {
        return service.listarAlertasAbertos();
    }

    @PatchMapping("/alertas/{id}/resolver")
    public AlertaResponse resolver(@PathVariable Long id) {
        return service.resolverAlerta(id);
    }

    @GetMapping("/indicadores")
    public IndicadoresResponse indicadores() {
        return service.indicadores();
    }
}
