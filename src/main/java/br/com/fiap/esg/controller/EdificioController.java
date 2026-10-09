package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.*;
import br.com.fiap.esg.service.MonitoramentoEnergeticoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edificios")
public class EdificioController {

    private final MonitoramentoEnergeticoService service;

    public EdificioController(MonitoramentoEnergeticoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EdificioResponse cadastrar(@Valid @RequestBody EdificioRequest req) {
        return service.cadastrarEdificio(req);
    }

    @GetMapping
    public List<EdificioResponse> listar() {
        return service.listarEdificios();
    }

    @GetMapping("/{id}")
    public EdificioResponse buscar(@PathVariable Long id) {
        return service.buscarEdificio(id);
    }

    @PostMapping("/{id}/medicoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicaoResponse registrarMedicao(@PathVariable Long id, @Valid @RequestBody MedicaoRequest req) {
        return service.registrarMedicao(id, req);
    }

    @GetMapping("/{id}/medicoes")
    public List<MedicaoResponse> listarMedicoes(@PathVariable Long id) {
        return service.listarMedicoes(id);
    }
}
