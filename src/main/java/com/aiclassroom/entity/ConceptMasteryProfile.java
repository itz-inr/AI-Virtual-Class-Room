package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "concept_mastery_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptMasteryProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private String topic;

    private int masteryScore;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private int attemptCount;

    private int correctCount;

    private LocalDateTime lastAttemptedAt;
}
