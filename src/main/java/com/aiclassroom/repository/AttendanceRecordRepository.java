package com.aiclassroom.repository;

import com.aiclassroom.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findBySession_Id(UUID sessionId);
    List<AttendanceRecord> findByStudent_Id(UUID studentId);
}