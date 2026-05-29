package com.aiclassroom.repository;

import com.aiclassroom.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByCourse_Id(UUID courseId);
    List<Session> findByIsActiveTrue();
    Optional<Session> findFirstByCourse_IdAndIsActiveTrueOrderByStartTimeDesc(UUID courseId);
}
