package br.com.fiap.esg.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Alerta gerado automaticamente quando o consumo diario excede a meta ESG. */
@Entity
@Table(name = "alerta_consumo")
public class AlertaConsumo {

    public enum Status { ABERTO, RESOLVIDO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "edificio_id")
    private Edificio edificio;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "consumo_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoKwh;

    @Column(name = "limite_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteKwh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ABERTO;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected AlertaConsumo() { }

    public AlertaConsumo(Edificio edificio, LocalDate dataReferencia, BigDecimal consumoKwh, BigDecimal limiteKwh) {
        this.edificio = edificio;
        this.dataReferencia = dataReferencia;
        this.consumoKwh = consumoKwh;
        this.limiteKwh = limiteKwh;
    }

    public Long getId() { return id; }
    public Edificio getEdificio() { return edificio; }
    public LocalDate getDataReferencia() { return dataReferencia; }
    public BigDecimal getConsumoKwh() { return consumoKwh; }
    public void setConsumoKwh(BigDecimal consumoKwh) { this.consumoKwh = consumoKwh; }
    public BigDecimal getLimiteKwh() { return limiteKwh; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
