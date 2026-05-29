package com.aiclassroom.repository;

import com.aiclassroom.entity.ConceptMasteryProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConceptMasteryProfileRepository extends JpaRepository<ConceptMasteryProfile, UUID> {

    Optional<ConceptMasteryProfile> findByStudent_IdAndCourse_IdAndTopic(UUID studentId, UUID courseId, String topic);

    List<ConceptMasteryProfile> findByStudent_IdAndCourse_Id(UUID studentId, UUID courseId);

    List<ConceptMasteryProfile> findByStudent_Id(UUID studentId);
}
