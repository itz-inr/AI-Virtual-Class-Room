package com.aiclassroom.repository;

import com.aiclassroom.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByCourse_Id(UUID courseId);
}