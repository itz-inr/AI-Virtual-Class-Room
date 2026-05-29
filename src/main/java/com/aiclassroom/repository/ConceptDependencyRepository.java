package com.aiclassroom.repository;

import com.aiclassroom.entity.ConceptDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConceptDependencyRepository extends JpaRepository<ConceptDependency, UUID> {
    List<ConceptDependency> findByCourseId(UUID courseId);

    List<ConceptDependency> findByCourseIdAndDependentTopic(UUID courseId, String dependentTopic);
}
