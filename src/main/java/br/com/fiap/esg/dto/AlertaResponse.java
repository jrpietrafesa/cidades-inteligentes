package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.AlertaConsumo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AlertaResponse(Long id, Long edificioId, String edificio, LocalDate dataReferencia,
                             BigDecimal consumoKwh, BigDecimal limiteKwh, String status) {
    public static AlertaResponse de(AlertaConsumo a) {
        return new AlertaResponse(a.getId(), a.getEdificio().getId(), a.getEdificio().getNome(),
                a.getDataReferencia(), a.getConsumoKwh(), a.getLimiteKwh(), a.getStatus().name());
    }
}
