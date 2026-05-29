package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Double aiScore;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    private LocalDateTime submittedAt;
}