package br.com.fiap.esg.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EdificioRequest(
        @NotBlank String nome,
        @NotBlank String cidade,
        @NotBlank String tipo,
        @NotNull @DecimalMin(value = "0.01") BigDecimal limiteDiarioKwh) { }
