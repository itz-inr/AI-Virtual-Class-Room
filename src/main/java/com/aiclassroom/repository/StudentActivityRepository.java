package com.aiclassroom.repository;

import com.aiclassroom.entity.StudentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentActivityRepository extends JpaRepository<StudentActivity, UUID> {
    List<StudentActivity> findBySession_Id(UUID sessionId);
    List<StudentActivity> findByStudent_Id(UUID studentId);
}