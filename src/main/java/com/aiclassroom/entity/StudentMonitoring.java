package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_monitoring")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMonitoring {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;
    
    private String activity; // ACTIVE, IDLE, TAKING_QUIZ, LEFT_CLASS
    private Integer focusScore; // 0-100
    private LocalDateTime timestamp;
    private String aiObservation;
}
