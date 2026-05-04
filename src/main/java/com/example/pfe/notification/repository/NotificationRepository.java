package com.example.pfe.notification.repository;

import com.example.pfe.notification.entity.Notification;
import com.example.pfe.notification.enums.NotificationStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(Long destinataireId);

    List<Notification> findByDestinataireIdAndStatutOrderByCreatedAtDesc(Long destinataireId, NotificationStatut statut);

    long countByDestinataireIdAndStatut(Long destinataireId, NotificationStatut statut);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.statut = :statut WHERE n.id = :id")
    void updateStatut(@Param("id") Long id, @Param("statut") NotificationStatut statut);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.statut = :statut WHERE n.destinataireId = :destinataireId")
    void marquerToutCommeLu(@Param("destinataireId") Long destinataireId, @Param("statut") NotificationStatut statut);
}