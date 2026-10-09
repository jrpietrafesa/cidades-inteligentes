package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.*;
import br.com.fiap.esg.exception.RecursoNaoEncontradoException;
import br.com.fiap.esg.model.AlertaConsumo;
import br.com.fiap.esg.model.Edificio;
import br.com.fiap.esg.model.MedicaoConsumo;
import br.com.fiap.esg.repository.AlertaConsumoRepository;
import br.com.fiap.esg.repository.EdificioRepository;
import br.com.fiap.esg.repository.MedicaoConsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonitoramentoEnergeticoService {

    /** Fator medio de emissao do Sistema Interligado Nacional (kgCO2/kWh) - referencia didatica. */
    static final BigDecimal FATOR_EMISSAO_KG_CO2_POR_KWH = new BigDecimal("0.0385");

    private final EdificioRepository edificios;
    private final MedicaoConsumoRepository medicoes;
    private final AlertaConsumoRepository alertas;

    public MonitoramentoEnergeticoService(EdificioRepository edificios,
                                          MedicaoConsumoRepository medicoes,
                                          AlertaConsumoRepository alertas) {
        this.edificios = edificios;
        this.medicoes = medicoes;
        this.alertas = alertas;
    }

    @Transactional
    public EdificioResponse cadastrarEdificio(EdificioRequest req) {
        Edificio salvo = edificios.save(new Edificio(req.nome(), req.cidade(), req.tipo(), req.limiteDiarioKwh()));
        return EdificioResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<EdificioResponse> listarEdificios() {
        return edificios.findAll().stream().map(EdificioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EdificioResponse buscarEdificio(Long id) {
        return EdificioResponse.de(obterEdificio(id));
    }

    /**
     * Registra uma leitura e, se o consumo acumulado do dia ultrapassar o limite ESG,
     * abre (ou atualiza) o alerta do dia para o edificio.
     */
    @Transactional
    public MedicaoResponse registrarMedicao(Long edificioId, MedicaoRequest req) {
        Edificio edificio = obterEdificio(edificioId);
        LocalDateTime dataHora = req.dataHora() != null ? req.dataHora() : LocalDateTime.now();
        MedicaoConsumo medicao = medicoes.save(new MedicaoConsumo(edificio, req.kwh(), dataHora));

        LocalDate dia = dataHora.toLocalDate();
        BigDecimal consumoDia = zeroSeNulo(
                medicoes.somarConsumo(edificioId, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay()));

        boolean excedeu = consumoDia.compareTo(edificio.getLimiteDiarioKwh()) > 0;
        if (excedeu) {
            AlertaConsumo alerta = alertas.findByEdificioIdAndDataReferencia(edificioId, dia)
                    .orElseGet(() -> new AlertaConsumo(edificio, dia, consumoDia, edificio.getLimiteDiarioKwh()));
            alerta.setConsumoKwh(consumoDia);
            alertas.save(alerta);
        }
        return MedicaoResponse.de(medicao, excedeu);
    }

    @Transactional(readOnly = true)
    public List<MedicaoResponse> listarMedicoes(Long edificioId) {
        obterEdificio(edificioId);
        return medicoes.findByEdificioIdOrderByDataHoraDesc(edificioId).stream()
                .map(m -> MedicaoResponse.de(m, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarAlertasAbertos() {
        return alertas.findByStatusOrderByCriadoEmDesc(AlertaConsumo.Status.ABERTO).stream()
                .map(AlertaResponse::de).toList();
    }

    @Transactional
    public AlertaResponse resolverAlerta(Long alertaId) {
        AlertaConsumo alerta = alertas.findById(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta " + alertaId + " nao encontrado"));
        alerta.setStatus(AlertaConsumo.Status.RESOLVIDO);
        return AlertaResponse.de(alertas.save(alerta));
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse indicadores() {
        BigDecimal total = zeroSeNulo(medicoes.somarConsumoTotal());
        return new IndicadoresResponse(
                edificios.count(),
                medicoes.count(),
                total,
                calcularEmissaoKgCo2(total),
                alertas.countByStatus(AlertaConsumo.Status.ABERTO),
                alertas.countByStatus(AlertaConsumo.Status.RESOLVIDO));
    }

    static BigDecimal calcularEmissaoKgCo2(BigDecimal kwh) {
        return kwh.multiply(FATOR_EMISSAO_KG_CO2_POR_KWH).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private Edificio obterEdificio(Long id) {
        return edificios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Edificio " + id + " nao encontrado"));
    }
}
