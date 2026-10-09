package br.com.fiap.esg.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Edificio publico monitorado (escola, hospital, prefeitura...). */
@Entity
@Table(name = "edificio")
public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 40)
    private String tipo;

    /** Limite diario de consumo em kWh definido pela meta ESG. */
    @Column(name = "limite_diario_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteDiarioKwh;

    protected Edificio() { }

    public Edificio(String nome, String cidade, String tipo, BigDecimal limiteDiarioKwh) {
        this.nome = nome;
        this.cidade = cidade;
        this.tipo = tipo;
        this.limiteDiarioKwh = limiteDiarioKwh;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getLimiteDiarioKwh() { return limiteDiarioKwh; }
    public void setLimiteDiarioKwh(BigDecimal limiteDiarioKwh) { this.limiteDiarioKwh = limiteDiarioKwh; }
}
