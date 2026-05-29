package com.aiclassroom.repository;

import com.aiclassroom.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByStudent_Id(UUID studentId);
    List<Enrollment> findByCourse_Id(UUID courseId);
    boolean existsByStudent_IdAndCourse_Id(UUID studentId, UUID courseId);
}