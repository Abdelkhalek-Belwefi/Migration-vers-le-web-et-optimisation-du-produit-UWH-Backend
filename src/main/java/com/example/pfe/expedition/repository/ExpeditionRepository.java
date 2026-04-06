package com.example.pfe.expedition.repository;

import com.example.pfe.expedition.entity.Expedition;
import com.example.pfe.expedition.entity.ExpeditionStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpeditionRepository extends JpaRepository<Expedition, Long> {
    List<Expedition> findByStatut(ExpeditionStatut statut);
    Optional<Expedition> findByCommandeId(Long commandeId);
}