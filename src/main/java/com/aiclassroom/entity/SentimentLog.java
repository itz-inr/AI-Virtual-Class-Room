package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sentiment_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    private String sentiment; // POSITIVE/NEUTRAL/NEGATIVE
    private Double confidence;
    private String rawText;
    private LocalDateTime timestamp;
}