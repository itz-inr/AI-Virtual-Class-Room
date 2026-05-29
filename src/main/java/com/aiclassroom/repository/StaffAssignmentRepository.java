package com.aiclassroom.repository;

import com.aiclassroom.entity.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, UUID> {
    List<StaffAssignment> findByStaff_Id(UUID staffId);

    List<StaffAssignment> findByAcademicClass_Id(UUID classId);

    Optional<StaffAssignment> findByStaff_IdAndSubject_IdAndAcademicClass_Id(UUID staffId, UUID subjectId,
            UUID classId);

    boolean existsByStaff_IdAndSubject_IdAndAcademicClass_Id(UUID staffId, UUID subjectId, UUID classId);
}
