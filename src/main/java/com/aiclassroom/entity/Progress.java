package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Progress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @Column(nullable = false)
    private Double completion = 0.0;
    
    private Double quizScore = 0.0;
    
    private Integer topicsLearned = 0;
    
    private Integer questionsAsked = 0;
    
    private Integer quizzesTaken = 0;
    
    private Double attendanceRate = 100.0;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
