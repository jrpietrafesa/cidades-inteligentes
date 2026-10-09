package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.AlertaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AlertaConsumoRepository extends JpaRepository<AlertaConsumo, Long> {

    Optional<AlertaConsumo> findByEdificioIdAndDataReferencia(Long edificioId, LocalDate dataReferencia);

    List<AlertaConsumo> findByStatusOrderByCriadoEmDesc(AlertaConsumo.Status status);

    long countByStatus(AlertaConsumo.Status status);
}
