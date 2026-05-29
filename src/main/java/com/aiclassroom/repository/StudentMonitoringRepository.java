package com.aiclassroom.repository;

import com.aiclassroom.entity.StudentMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentMonitoringRepository extends JpaRepository<StudentMonitoring, UUID> {
    List<StudentMonitoring> findBySession_Id(UUID sessionId);
    List<StudentMonitoring> findByStudent_IdAndSession_Id(UUID studentId, UUID sessionId);
}
