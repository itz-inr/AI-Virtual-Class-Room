package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "staff_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "staff_id", "subject_id", "class_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StaffAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_id")
    private User staff;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(optional = false)
    @JoinColumn(name = "class_id")
    private AcademicClass academicClass;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
