package com.aiclassroom.repository;

import com.aiclassroom.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByTeacher_Id(UUID teacherId);

    List<Course> findByActiveTrue();

    List<Course> findByAcademicClass_Id(UUID classId);

    List<Course> findByAcademicClass_IdAndActiveTrue(UUID classId);
}
