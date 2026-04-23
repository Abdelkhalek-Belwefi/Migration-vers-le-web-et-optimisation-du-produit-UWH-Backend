package com.example.pfe.livraison.repository;

import com.example.pfe.auth.entity.User;
import com.example.pfe.livraison.entity.Livraison;
import com.example.pfe.livraison.entity.LivraisonStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByTransporteurAndStatutIn(User transporteur, List<LivraisonStatut> statuts);
    Optional<Livraison> findByExpeditionId(Long expeditionId);
}