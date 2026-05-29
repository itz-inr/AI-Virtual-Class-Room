package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User sender;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    private boolean isAiResponse = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
