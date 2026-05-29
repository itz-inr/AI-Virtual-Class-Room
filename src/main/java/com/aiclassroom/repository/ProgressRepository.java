package com.aiclassroom.repository;

import com.aiclassroom.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, UUID> {
    Optional<Progress> findByUser_IdAndCourse_Id(UUID userId, UUID courseId);
    List<Progress> findByUser_Id(UUID userId);
    List<Progress> findByCourse_Id(UUID courseId);
}
