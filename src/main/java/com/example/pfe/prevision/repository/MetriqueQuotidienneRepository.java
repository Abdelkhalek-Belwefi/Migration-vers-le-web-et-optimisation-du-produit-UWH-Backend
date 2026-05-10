package com.example.pfe.prevision.repository;

import com.example.pfe.prevision.entity.MetriqueQuotidienne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetriqueQuotidienneRepository extends JpaRepository<MetriqueQuotidienne, Long> {

    Optional<MetriqueQuotidienne> findByEntrepotIdAndDateMetrique(Long entrepotId, LocalDate dateMetrique);

    List<MetriqueQuotidienne> findByEntrepotIdOrderByDateMetriqueAsc(Long entrepotId);

    List<MetriqueQuotidienne> findByEntrepotIdAndDateMetriqueBetweenOrderByDateMetriqueAsc(
            Long entrepotId, LocalDate dateDebut, LocalDate dateFin);

    boolean existsByEntrepotIdAndDateMetrique(Long entrepotId, LocalDate dateMetrique);

    @Query("SELECT m FROM MetriqueQuotidienne m WHERE m.entrepot.id = :entrepotId AND m.dateMetrique >= :dateDebut ORDER BY m.dateMetrique ASC")
    List<MetriqueQuotidienne> getHistoriqueDepuis(@Param("entrepotId") Long entrepotId, @Param("dateDebut") LocalDate dateDebut);

    @Query("SELECT AVG(m.chargeTravail) FROM MetriqueQuotidienne m WHERE m.entrepot.id = :entrepotId AND m.dateMetrique >= :dateDebut")
    Double getChargeMoyenneDepuis(@Param("entrepotId") Long entrepotId, @Param("dateDebut") LocalDate dateDebut);
}