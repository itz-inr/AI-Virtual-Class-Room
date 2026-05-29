package com.aiclassroom.repository;

import com.aiclassroom.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findBySession_IdOrderByTimestampAsc(UUID sessionId);
}
