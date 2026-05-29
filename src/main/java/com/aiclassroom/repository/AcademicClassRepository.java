package com.aiclassroom.repository;

import com.aiclassroom.entity.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademicClassRepository extends JpaRepository<AcademicClass, UUID> {
    List<AcademicClass> findByDepartment_Id(UUID departmentId);
}
