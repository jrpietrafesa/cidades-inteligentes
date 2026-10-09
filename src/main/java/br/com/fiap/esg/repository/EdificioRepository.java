package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdificioRepository extends JpaRepository<Edificio, Long> { }
