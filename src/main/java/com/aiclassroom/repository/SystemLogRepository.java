package com.aiclassroom.repository;

import com.aiclassroom.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, UUID> {
    List<SystemLog> findAllByOrderByTimestampDesc();
}