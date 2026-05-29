package com.aiclassroom.repository;

import com.aiclassroom.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findByAssignment_Id(UUID assignmentId);
    List<Submission> findByStudent_Id(UUID studentId);
}