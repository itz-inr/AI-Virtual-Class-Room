package com.aiclassroom.job;

import com.aiclassroom.entity.*;
import com.aiclassroom.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskAnalysisJob {

    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ConceptMasteryProfileRepository conceptMasteryProfileRepository;
    private final StudentActivityRepository studentActivityRepository;
    private final StudentRiskProfileRepository studentRiskProfileRepository;
    private final RecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void computeRiskProfiles() {
        List<User> students = userRepository.findByRole(User.Role.STUDENT);

        for (User student : students) {
            UUID studentId = student.getId();

            // Attendance Rate
            List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByStudent_Id(studentId);
            double attendanceRate = 100.0;
            if (!attendanceRecords.isEmpty()) {
                long presentCount = attendanceRecords.stream().filter(AttendanceRecord::isPresent).count();
                attendanceRate = (presentCount * 100.0) / attendanceRecords.size();
            }

            // Average Mastery Score & Weak Topics
            List<ConceptMasteryProfile> profiles = conceptMasteryProfileRepository.findByStudent_Id(studentId);

            double avgMasteryScore = 100.0;
            if (!profiles.isEmpty()) {
                avgMasteryScore = profiles.stream().mapToInt(ConceptMasteryProfile::getMasteryScore).average()
                        .orElse(100.0);
            }

            // Engagement Score
            List<StudentActivity> activities = studentActivityRepository.findByStudent_Id(studentId);
            double engagementScore = 100.0;
            if (!activities.isEmpty()) {
                engagementScore = activities.stream()
                        .mapToDouble(a -> a.getParticipationScore() != null ? a.getParticipationScore() : 50.0)
                        .average().orElse(100.0);
            }

            // Compute Risk Score
            int riskScore = (int) Math.round((100 - attendanceRate) * 0.3
                    + (100 - avgMasteryScore) * 0.4
                    + (100 - engagementScore) * 0.3);
            riskScore = Math.max(0, Math.min(100, riskScore));

            RiskLevel riskLevel;
            if (riskScore < 30)
                riskLevel = RiskLevel.LOW;
            else if (riskScore < 70)
                riskLevel = RiskLevel.MEDIUM;
            else
                riskLevel = RiskLevel.HIGH;

            Map<String, Object> riskFactors = new HashMap<>();
            riskFactors.put("attendanceRate", attendanceRate);
            riskFactors.put("avgMasteryScore", avgMasteryScore);
            riskFactors.put("engagementScore", engagementScore);

            String riskFactorsJson = "{}";
            try {
                riskFactorsJson = objectMapper.writeValueAsString(riskFactors);
            } catch (JsonProcessingException e) {
                // Ignore
            }

            StudentRiskProfile riskProfile = studentRiskProfileRepository.findByStudentId(studentId)
                    .orElse(new StudentRiskProfile());
            riskProfile.setStudent(student);
            riskProfile.setRiskScore(riskScore);
            riskProfile.setRiskLevel(riskLevel);
            riskProfile.setRiskFactors(riskFactorsJson);
            riskProfile.setComputedAt(LocalDateTime.now());
            studentRiskProfileRepository.save(riskProfile);

            // Generate Recommendations for weak topics
            for (ConceptMasteryProfile profile : profiles) {
                if (profile.getMasteryScore() < 60) {
                    Recommendation recommendation = new Recommendation();
                    recommendation.setStudent(student);
                    recommendation.setCourse(profile.getCourse());
                    recommendation.setType(RecommendationType.MICRO_REVISION);
                    recommendation.setReason(
                            "Low mastery score (" + profile.getMasteryScore() + ") in topic: " + profile.getTopic());
                    recommendation.setScore((double) (100 - profile.getMasteryScore()));
                    recommendation.setCreatedAt(LocalDateTime.now());
                    recommendationRepository.save(recommendation);
                }
            }
        }
    }
}
