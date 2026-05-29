package com.aiclassroom.repository;

import com.aiclassroom.entity.SentimentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentimentLogRepository extends JpaRepository<SentimentLog, UUID> {
    List<SentimentLog> findBySession_Id(UUID sessionId);
    List<SentimentLog> findByStudent_Id(UUID studentId);
}