package com.aiclassroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "concept_dependencies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptDependency {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private String prerequisiteTopic;

    @Column(nullable = false)
    private String dependentTopic;
}
