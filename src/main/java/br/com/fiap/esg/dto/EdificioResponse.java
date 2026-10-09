package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.Edificio;
import java.math.BigDecimal;

public record EdificioResponse(Long id, String nome, String cidade, String tipo, BigDecimal limiteDiarioKwh) {
    public static EdificioResponse de(Edificio e) {
        return new EdificioResponse(e.getId(), e.getNome(), e.getCidade(), e.getTipo(), e.getLimiteDiarioKwh());
    }
}
