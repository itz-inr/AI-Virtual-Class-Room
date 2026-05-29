package com.aiclassroom.service;

import com.aiclassroom.entity.ConceptDependency;
import com.aiclassroom.entity.ConceptMasteryProfile;
import com.aiclassroom.repository.ConceptDependencyRepository;
import com.aiclassroom.repository.ConceptMasteryProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NextBestTopicService {

    private final ConceptMasteryProfileRepository conceptMasteryProfileRepository;
    private final ConceptDependencyRepository conceptDependencyRepository;

    public String getNextBestTopic(UUID studentId, UUID courseId) {
        List<ConceptMasteryProfile> profiles = conceptMasteryProfileRepository.findByStudent_IdAndCourse_Id(studentId,
                courseId);

        List<ConceptMasteryProfile> profilesToImprove = profiles.stream()
                .filter(p -> p.getMasteryScore() < 70)
                .collect(Collectors.toList());

        List<ConceptDependency> dependencies = conceptDependencyRepository.findByCourseId(courseId);

        String bestTopic = null;
        int lowestMastery = Integer.MAX_VALUE;

        // Find topics where all prerequisites have mastery >= 70
        for (ConceptMasteryProfile profile : profilesToImprove) {
            String topic = profile.getTopic();
            boolean isEligible = true;

            List<ConceptDependency> topicDependencies = dependencies.stream()
                    .filter(d -> d.getDependentTopic().equals(topic))
                    .collect(Collectors.toList());

            for (ConceptDependency dep : topicDependencies) {
                Optional<ConceptMasteryProfile> prereqProfile = profiles.stream()
                        .filter(p -> p.getTopic().equals(dep.getPrerequisiteTopic()))
                        .findFirst();

                if (prereqProfile.isEmpty() || prereqProfile.get().getMasteryScore() < 70) {
                    isEligible = false;
                    break;
                }
            }

            if (isEligible) {
                if (profile.getMasteryScore() < lowestMastery) {
                    lowestMastery = profile.getMasteryScore();
                    bestTopic = topic;
                }
            }
        }

        return bestTopic;
    }
}
