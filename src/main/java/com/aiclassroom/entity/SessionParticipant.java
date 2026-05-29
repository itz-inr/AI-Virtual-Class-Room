package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private Boolean isMuted = false;
    @Builder.Default
    private Boolean isVideoOn = true;
    @Builder.Default
    private Boolean isHandRaised = false;
    private String lastReaction;

    private LocalDateTime lastSeen;
    @Builder.Default
    private Boolean isApproved = false;

    @Builder.Default
    private Boolean hasLeft = false;
}
