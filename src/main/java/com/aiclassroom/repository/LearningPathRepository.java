package com.aiclassroom.repository;

import com.aiclassroom.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, UUID> {
    Optional<LearningPath> findByStudent_IdAndCourse_Id(UUID studentId, UUID courseId);
}