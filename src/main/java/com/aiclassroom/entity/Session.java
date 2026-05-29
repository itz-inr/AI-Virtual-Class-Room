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
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    @Column(nullable = false)
    private String sessionName;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private Boolean isActive = false;
    
    private Integer participantCount = 0;
    
    @Enumerated(EnumType.STRING)
    private AiMode aiMode = AiMode.DISABLED;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
