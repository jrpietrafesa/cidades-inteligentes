package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.MedicaoConsumo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicaoResponse(Long id, Long edificioId, BigDecimal kwh, LocalDateTime dataHora, boolean alertaGerado) {
    public static MedicaoResponse de(MedicaoConsumo m, boolean alertaGerado) {
        return new MedicaoResponse(m.getId(), m.getEdificio().getId(), m.getKwh(), m.getDataHora(), alertaGerado);
    }
}
