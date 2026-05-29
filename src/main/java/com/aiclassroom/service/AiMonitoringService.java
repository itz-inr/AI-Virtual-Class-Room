package com.aiclassroom.service;

import com.aiclassroom.entity.StudentMonitoring;
import com.aiclassroom.entity.User;
import com.aiclassroom.entity.Session;
import com.aiclassroom.repository.StudentMonitoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiMonitoringService {

    private final StudentMonitoringRepository monitoringRepository;
    private final BytezAiService aiService;
    private final SimpMessagingTemplate messagingTemplate;

    public void trackStudentActivity(User student, Session session, String activity) {
        StudentMonitoring monitoring = StudentMonitoring.builder()
                .student(student)
                .session(session)
                .activity(activity)
                .focusScore(calculateFocusScore(activity))
                .timestamp(LocalDateTime.now())
                .aiObservation(generateAiObservation(activity))
                .build();

        if (monitoring != null) {
            monitoringRepository.save(monitoring);
            broadcastInsights(session.getId());
        }
    }

    private void broadcastInsights(UUID sessionId) {
        Map<String, Object> insights = getClassroomInsights(sessionId);
        if (insights != null) {
            messagingTemplate.convertAndSend("/topic/classroom/" + sessionId + "/monitoring", insights);
        }
    }

    public Map<String, Object> getClassroomInsights(UUID sessionId) {
        List<StudentMonitoring> activities = monitoringRepository.findBySession_Id(sessionId);

        long activeStudents = activities.stream()
                .filter(a -> "ACTIVE".equals(a.getActivity()))
                .count();

        long takingQuiz = activities.stream()
                .filter(a -> "TAKING_QUIZ".equals(a.getActivity()))
                .count();

        long idle = activities.stream()
                .filter(a -> "IDLE".equals(a.getActivity()))
                .count();

        double avgFocus = activities.stream()
                .mapToInt(StudentMonitoring::getFocusScore)
                .average()
                .orElse(0.0);

        String aiSummary = aiService.getAiResponse(
                "Summarize classroom status: " + activeStudents + " active, " +
                        takingQuiz + " taking quiz, " + idle + " idle students. Average focus: " + avgFocus);

        Map<String, Object> insights = new HashMap<>();
        insights.put("activeStudents", activeStudents);
        insights.put("takingQuiz", takingQuiz);
        insights.put("idleStudents", idle);
        insights.put("averageFocus", avgFocus);
        insights.put("aiSummary", aiSummary);
        insights.put("needsAttention", idle > activeStudents);

        return insights;
    }

    private int calculateFocusScore(String activity) {
        return switch (activity) {
            case "ACTIVE" -> 90;
            case "TAKING_QUIZ" -> 100;
            case "IDLE" -> 30;
            case "LEFT_CLASS" -> 0;
            default -> 50;
        };
    }

    private String generateAiObservation(String activity) {
        return switch (activity) {
            case "ACTIVE" -> "Student is engaged and participating";
            case "TAKING_QUIZ" -> "Student is focused on assessment";
            case "IDLE" -> "Student may need attention";
            case "LEFT_CLASS" -> "Student has left the session";
            default -> "Monitoring student activity";
        };
    }
}
