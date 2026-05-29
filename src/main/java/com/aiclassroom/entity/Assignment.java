package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private AiMode gradingMode;
}