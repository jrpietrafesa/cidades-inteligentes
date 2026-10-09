package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.MedicaoConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Somas retornam null quando nao ha leituras; o servico trata como zero. */

public interface MedicaoConsumoRepository extends JpaRepository<MedicaoConsumo, Long> {

    List<MedicaoConsumo> findByEdificioIdOrderByDataHoraDesc(Long edificioId);

    @Query("""
           select sum(m.kwh) from MedicaoConsumo m
           where m.edificio.id = :edificioId
             and m.dataHora >= :inicio and m.dataHora < :fim
           """)
    BigDecimal somarConsumo(@Param("edificioId") Long edificioId,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fim") LocalDateTime fim);

    @Query("select sum(m.kwh) from MedicaoConsumo m")
    BigDecimal somarConsumoTotal();
}
