package com.aiclassroom.repository;

import com.aiclassroom.entity.StudentRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRiskProfileRepository extends JpaRepository<StudentRiskProfile, UUID> {
    Optional<StudentRiskProfile> findByStudentId(UUID studentId);
}
