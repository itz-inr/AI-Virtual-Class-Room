package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_risk_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRiskProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    private int riskScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String riskFactors; // JSON string

    private LocalDateTime computedAt;
}
