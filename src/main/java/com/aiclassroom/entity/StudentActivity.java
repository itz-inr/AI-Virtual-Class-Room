package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private Double participationScore;
}