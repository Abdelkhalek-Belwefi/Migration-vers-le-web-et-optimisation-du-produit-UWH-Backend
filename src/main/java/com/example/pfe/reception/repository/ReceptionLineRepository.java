package com.example.pfe.reception.repository;

import com.example.pfe.reception.entity.ReceptionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceptionLineRepository extends JpaRepository<ReceptionLine, Long> {

    List<ReceptionLine> findByReceptionId(Long receptionId);

    List<ReceptionLine> findByArticleId(Long articleId);
}