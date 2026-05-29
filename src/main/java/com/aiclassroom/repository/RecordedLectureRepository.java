package com.aiclassroom.repository;

import com.aiclassroom.entity.RecordedLecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordedLectureRepository extends JpaRepository<RecordedLecture, UUID> {
    List<RecordedLecture> findBySession_Id(UUID sessionId);
}