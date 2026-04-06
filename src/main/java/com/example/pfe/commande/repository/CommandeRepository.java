package com.example.pfe.commande.repository;

import com.example.pfe.commande.entity.Commande;
import com.example.pfe.commande.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByStatut(StatutCommande statut);

    @Query("SELECT c FROM Commande c WHERE c.statut = :statut AND NOT EXISTS (SELECT e FROM Expedition e WHERE e.commande = c)")
    List<Commande> findByStatutAndNoExpedition(@Param("statut") StatutCommande statut);
}