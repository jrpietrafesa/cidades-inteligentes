package br.com.fiap.esg.bdd;

import io.cucumber.java.ParameterType;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Passos de negocio do monitoramento energetico (edificios, medicoes, alertas, indicadores). */
public class MonitoramentoSteps {

    private static final long ID_INEXISTENTE = 999_999_999L;
    private static final BigDecimal FATOR_EMISSAO = new BigDecimal("0.0385");
    private static final String AUSENTE = "ausente";

    private final ContextoCenario contexto;

    public MonitoramentoSteps(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    /** Numero sempre com ponto decimal, independente do idioma do arquivo .feature. */
    @ParameterType("-?\\d+(?:\\.\\d+)?")
    public BigDecimal kwh(String valor) {
        return new BigDecimal(valor);
    }

    // ---------- Edificios ----------

    @Dado("que existe um edifício {string} com meta diária de {kwh} kWh")
    public void existeEdificio(String nome, BigDecimal limite) {
        cadastrar(corpoEdificio(nome, "Sao Paulo", "HOSPITAL", limite.toPlainString()));
        assertThat(contexto.resposta.statusCode()).isEqualTo(201);
        contexto.edificioId = contexto.resposta.jsonPath().getLong("id");
    }

    @Quando("cadastro o edifício {string} do tipo {string} em {string} com meta diária de {kwh} kWh")
    public void cadastroEdificio(String nome, String tipo, String cidade, BigDecimal limite) {
        cadastrar(corpoEdificio(nome, cidade, tipo, limite.toPlainString()));
    }

    @Quando("tento cadastrar um edifício com nome {string}, cidade {string}, tipo {string} e meta {string}")
    public void tentoCadastrar(String nome, String cidade, String tipo, String limite) {
        cadastrar(corpoEdificio(nome, cidade, tipo, limite));
    }

    @Quando("envio um cadastro de edifício com JSON malformado")
    public void jsonMalformado() {
        contexto.resposta = contexto.api().body("{\"nome\": ").post("/api/edificios");
    }

    @Quando("consulto o edifício cadastrado")
    public void consultoEdificio() {
        contexto.resposta = contexto.api().get("/api/edificios/{id}", contexto.edificioId);
    }

    @Quando("consulto um edifício que não existe")
    public void consultoEdificioInexistente() {
        contexto.resposta = contexto.api().get("/api/edificios/{id}", ID_INEXISTENTE);
    }

    @Quando("consulto um edifício com identificador {string}")
    public void consultoEdificioIdentificador(String identificador) {
        contexto.resposta = contexto.api().get("/api/edificios/" + identificador);
    }

    @Quando("listo os edifícios")
    public void listoEdificios() {
        contexto.resposta = contexto.api().get("/api/edificios");
    }

    @E("a lista deve conter o edifício cadastrado")
    public void listaContemEdificio() {
        assertThat(contexto.resposta.jsonPath().getList("id", Long.class)).contains(contexto.edificioId);
    }

    // ---------- Medicoes ----------

    @Dado("que foi registrada uma medição de {kwh} kWh em {string}")
    public void medicaoRegistrada(BigDecimal kwh, String dataHora) {
        registroMedicao(kwh, dataHora);
        assertThat(contexto.resposta.statusCode()).isEqualTo(201);
    }

    @Quando("registro uma medição de {kwh} kWh em {string}")
    public void registroMedicao(BigDecimal kwh, String dataHora) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("kwh", kwh);
        corpo.put("dataHora", dataHora);
        contexto.resposta = contexto.api().body(corpo)
                .post("/api/edificios/{id}/medicoes", contexto.edificioId);
    }

    @Quando("registro uma medição de {kwh} kWh sem informar a data")
    public void registroMedicaoSemData(BigDecimal kwh) {
        contexto.resposta = contexto.api().body(Map.of("kwh", kwh))
                .post("/api/edificios/{id}/medicoes", contexto.edificioId);
    }

    @Quando("tento registrar uma medição com valor {string}")
    public void tentoRegistrarMedicao(String kwh) {
        Map<String, Object> corpo = new HashMap<>();
        if (!AUSENTE.equals(kwh)) {
            corpo.put("kwh", new BigDecimal(kwh));
        }
        contexto.resposta = contexto.api().body(corpo)
                .post("/api/edificios/{id}/medicoes", contexto.edificioId);
    }

    @Quando("registro uma medição em um edifício que não existe")
    public void registroMedicaoInexistente() {
        contexto.resposta = contexto.api().body(Map.of("kwh", 10))
                .post("/api/edificios/{id}/medicoes", ID_INEXISTENTE);
    }

    @Quando("listo as medições do edifício")
    public void listoMedicoes() {
        contexto.resposta = contexto.api().get("/api/edificios/{id}/medicoes", contexto.edificioId);
    }

    @Quando("listo as medições de um edifício que não existe")
    public void listoMedicoesInexistente() {
        contexto.resposta = contexto.api().get("/api/edificios/{id}/medicoes", ID_INEXISTENTE);
    }

    @E("a lista deve ter {int} medições, da mais recente para a mais antiga")
    public void listaOrdenada(int quantidade) {
        List<String> datas = contexto.resposta.jsonPath().getList("dataHora", String.class);
        assertThat(datas).hasSize(quantidade).isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    // ---------- Alertas ----------

    @Quando("consulto os alertas abertos")
    public void consultoAlertas() {
        contexto.resposta = contexto.api().get("/api/alertas");
    }

    @Dado("que o alerta do edifício foi aberto")
    public void alertaAberto() {
        consultoAlertas();
        assertThat(contexto.resposta.statusCode()).isEqualTo(200);
        contexto.alertaId = Long.valueOf(alertaDoEdificio().get("id").toString());
    }

    @Quando("resolvo o alerta do edifício")
    public void resolvoAlerta() {
        contexto.resposta = contexto.api().patch("/api/alertas/{id}/resolver", contexto.alertaId);
    }

    @Dado("que o alerta do edifício foi resolvido")
    public void alertaResolvido() {
        alertaAberto();
        resolvoAlerta();
        assertThat(contexto.resposta.statusCode()).isEqualTo(200);
    }

    @Quando("resolvo um alerta que não existe")
    public void resolvoAlertaInexistente() {
        contexto.resposta = contexto.api().patch("/api/alertas/{id}/resolver", ID_INEXISTENTE);
    }

    @E("o alerta do edifício deve registrar consumo de {kwh} kWh contra a meta de {kwh} kWh na data {string}")
    public void alertaRastreavel(BigDecimal consumo, BigDecimal limite, String data) {
        Map<String, Object> alerta = alertaDoEdificio();
        assertThat(new BigDecimal(alerta.get("consumoKwh").toString())).isEqualByComparingTo(consumo);
        assertThat(new BigDecimal(alerta.get("limiteKwh").toString())).isEqualByComparingTo(limite);
        assertThat(alerta.get("dataReferencia")).isEqualTo(data);
        assertThat(alerta.get("status")).isEqualTo("ABERTO");
    }

    @E("o edifício não deve ter alerta aberto")
    public void semAlertaAberto() {
        consultoAlertas();
        assertThat(alertasAbertosDoEdificio()).isEmpty();
    }

    // ---------- Indicadores e sistema ----------

    @Dado("que anotei os indicadores atuais")
    public void anotoIndicadores() {
        consultoIndicadores();
        assertThat(contexto.resposta.statusCode()).isEqualTo(200);
        for (String campo : List.of("edificiosMonitorados", "medicoesRegistradas", "consumoTotalKwh",
                "alertasAbertos", "alertasResolvidos")) {
            contexto.indicadoresIniciais.put(campo, contexto.numero(campo));
        }
    }

    @Quando("consulto os indicadores")
    public void consultoIndicadores() {
        contexto.resposta = contexto.api().get("/api/indicadores");
    }

    @E("o indicador {string} deve ter variado {kwh}")
    public void indicadorVariou(String campo, BigDecimal variacao) {
        BigDecimal inicial = contexto.indicadoresIniciais.get(campo);
        assertThat(contexto.numero(campo).subtract(inicial)).isEqualByComparingTo(variacao);
    }

    @E("a emissão estimada deve corresponder ao consumo total pelo fator 0.0385 kgCO2 por kWh")
    public void emissaoConsistente() {
        BigDecimal esperada = contexto.numero("consumoTotalKwh").multiply(FATOR_EMISSAO)
                .setScale(2, RoundingMode.HALF_UP);
        assertThat(contexto.numero("emissaoEstimadaKgCo2")).isEqualByComparingTo(esperada);
    }

    @Quando("consulto a identificação da aplicação")
    public void consultoInfo() {
        contexto.resposta = contexto.api().get("/api/info");
    }

    @Quando("consulto a saúde da aplicação")
    public void consultoSaude() {
        contexto.resposta = contexto.api().get("/actuator/health");
    }

    // ---------- Apoio ----------

    private void cadastrar(Map<String, Object> corpo) {
        contexto.resposta = contexto.api().body(corpo).post("/api/edificios");
        if (contexto.resposta.statusCode() == 201) {
            contexto.edificioId = contexto.resposta.jsonPath().getLong("id");
        }
    }

    private Map<String, Object> corpoEdificio(String nome, String cidade, String tipo, String limite) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("nome", nome);
        corpo.put("cidade", cidade);
        corpo.put("tipo", tipo);
        if (!AUSENTE.equals(limite)) {
            corpo.put("limiteDiarioKwh", new BigDecimal(limite));
        }
        return corpo;
    }

    private List<Map<String, Object>> alertasAbertosDoEdificio() {
        List<Map<String, Object>> alertas = contexto.resposta.jsonPath().getList("$");
        return alertas.stream()
                .filter(a -> Long.valueOf(a.get("edificioId").toString()).equals(contexto.edificioId))
                .toList();
    }

    private Map<String, Object> alertaDoEdificio() {
        List<Map<String, Object>> doEdificio = alertasAbertosDoEdificio();
        assertThat(doEdificio).as("alerta aberto do edificio %s", contexto.edificioId).hasSize(1);
        return doEdificio.get(0);
    }
}
