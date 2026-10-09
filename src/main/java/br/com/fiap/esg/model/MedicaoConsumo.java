package br.com.fiap.esg.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Leitura de consumo energetico enviada pelo medidor inteligente (IoT). */
@Entity
@Table(name = "medicao_consumo")
public class MedicaoConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "edificio_id")
    private Edificio edificio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal kwh;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected MedicaoConsumo() { }

    public MedicaoConsumo(Edificio edificio, BigDecimal kwh, LocalDateTime dataHora) {
        this.edificio = edificio;
        this.kwh = kwh;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public Edificio getEdificio() { return edificio; }
    public BigDecimal getKwh() { return kwh; }
    public LocalDateTime getDataHora() { return dataHora; }
}
