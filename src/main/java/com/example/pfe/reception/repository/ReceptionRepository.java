package com.example.pfe.reception.repository;

import com.example.pfe.reception.entity.Reception;
import com.example.pfe.reception.entity.ReceptionStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {

    // ✅ Retourne Optional (0 ou 1 résultat)
    Optional<Reception> findByNumeroPO(String numeroPO);

    List<Reception> findByStatut(ReceptionStatut statut);

    List<Reception> findByDateReceptionBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT r FROM Reception r WHERE " +
            "(:numeroPO IS NULL OR r.numeroPO LIKE %:numeroPO%) AND " +
            "(:fournisseur IS NULL OR r.fournisseur LIKE %:fournisseur%) AND " +
            "(:statut IS NULL OR r.statut = :statut)")
    List<Reception> searchReceptions(@Param("numeroPO") String numeroPO,
                                     @Param("fournisseur") String fournisseur,
                                     @Param("statut") ReceptionStatut statut);
}