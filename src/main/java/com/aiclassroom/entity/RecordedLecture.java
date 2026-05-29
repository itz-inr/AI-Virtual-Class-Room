package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recorded_lectures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordedLecture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    private LocalDateTime recordedAt;
}