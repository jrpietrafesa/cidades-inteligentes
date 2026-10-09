package br.com.fiap.esg.dto;

import java.math.BigDecimal;

/** Resumo de indicadores ESG (pilar Ambiental) para o painel da cidade. */
public record IndicadoresResponse(long edificiosMonitorados, long medicoesRegistradas,
                                  BigDecimal consumoTotalKwh, BigDecimal emissaoEstimadaKgCo2,
                                  long alertasAbertos, long alertasResolvidos) { }
