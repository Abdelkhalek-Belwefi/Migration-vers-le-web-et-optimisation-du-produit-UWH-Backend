package com.example.pfe.reception.repository;

import com.example.pfe.reception.entity.PutawayTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long> {

    List<PutawayTask> findByStatut(String statut);

    List<PutawayTask> findByReceptionId(Long receptionId);

    List<PutawayTask> findByEmplacementDestination(String emplacement);
}