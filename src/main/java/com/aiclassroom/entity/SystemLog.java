package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String level;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}