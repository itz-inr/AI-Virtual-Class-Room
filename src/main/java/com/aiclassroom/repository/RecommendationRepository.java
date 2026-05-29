package com.aiclassroom.repository;

import com.aiclassroom.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findByStudent_IdOrderByScoreDesc(UUID studentId);
}