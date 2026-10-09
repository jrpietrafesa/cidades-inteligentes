package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.MedicaoRequest;
import br.com.fiap.esg.dto.MedicaoResponse;
import br.com.fiap.esg.exception.RecursoNaoEncontradoException;
import br.com.fiap.esg.model.AlertaConsumo;
import br.com.fiap.esg.model.Edificio;
import br.com.fiap.esg.model.MedicaoConsumo;
import br.com.fiap.esg.repository.AlertaConsumoRepository;
import br.com.fiap.esg.repository.EdificioRepository;
import br.com.fiap.esg.repository.MedicaoConsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoramentoEnergeticoServiceTest {

    @Mock EdificioRepository edificios;
    @Mock MedicaoConsumoRepository medicoes;
    @Mock AlertaConsumoRepository alertas;
    @InjectMocks MonitoramentoEnergeticoService service;

    Edificio escola;

    @BeforeEach
    void setUp() {
        escola = new Edificio("EMEF Jardim Verde", "Sao Paulo", "ESCOLA", new BigDecimal("100.00"));
        escola.setId(1L);
        lenient().when(edificios.findById(1L)).thenReturn(Optional.of(escola));
        lenient().when(medicoes.save(any(MedicaoConsumo.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Consumo dentro da meta ESG nao gera alerta")
    void consumoDentroDoLimite() {
        when(medicoes.somarConsumo(eq(1L), any(), any())).thenReturn(new BigDecimal("80.00"));

        MedicaoResponse resp = service.registrarMedicao(1L,
                new MedicaoRequest(new BigDecimal("30.00"), LocalDateTime.of(2026, 10, 1, 10, 0)));

        assertThat(resp.alertaGerado()).isFalse();
        verify(alertas, never()).save(any(AlertaConsumo.class));
    }

    @Test
    @DisplayName("Consumo acima da meta ESG abre alerta do dia")
    void consumoAcimaDoLimiteGeraAlerta() {
        when(medicoes.somarConsumo(eq(1L), any(), any())).thenReturn(new BigDecimal("120.00"));
        when(alertas.findByEdificioIdAndDataReferencia(eq(1L), any())).thenReturn(Optional.empty());

        MedicaoResponse resp = service.registrarMedicao(1L,
                new MedicaoRequest(new BigDecimal("50.00"), LocalDateTime.of(2026, 10, 1, 15, 0)));

        assertThat(resp.alertaGerado()).isTrue();
        ArgumentCaptor<AlertaConsumo> captor = ArgumentCaptor.forClass(AlertaConsumo.class);
        verify(alertas).save(captor.capture());
        assertThat(captor.getValue().getConsumoKwh()).isEqualByComparingTo("120.00");
        assertThat(captor.getValue().getStatus()).isEqualTo(AlertaConsumo.Status.ABERTO);
    }

    @Test
    @DisplayName("Edificio inexistente lanca excecao")
    void edificioInexistente() {
        when(edificios.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarMedicao(99L, new MedicaoRequest(BigDecimal.TEN, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Emissao de CO2 calculada com fator do SIN")
    void calculoEmissao() {
        assertThat(MonitoramentoEnergeticoService.calcularEmissaoKgCo2(new BigDecimal("1000")))
                .isEqualByComparingTo("38.50");
    }
}
