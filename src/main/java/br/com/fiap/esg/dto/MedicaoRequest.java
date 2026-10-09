package br.com.fiap.esg.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** dataHora e opcional: se ausente, usa o horario atual. */
public record MedicaoRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal kwh,
        LocalDateTime dataHora) { }
