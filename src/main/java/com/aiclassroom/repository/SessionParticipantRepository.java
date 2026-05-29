package com.aiclassroom.repository;

import com.aiclassroom.entity.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, UUID> {
    List<SessionParticipant> findBySession_IdAndHasLeftFalse(UUID sessionId);

    Optional<SessionParticipant> findBySession_IdAndUser_Id(UUID sessionId, UUID userId);
}
