package com.aiclassroom.controller;

import com.aiclassroom.service.AiMonitoringService;
import com.aiclassroom.repository.UserRepository;
import com.aiclassroom.repository.SessionRepository;
import com.aiclassroom.entity.User;
import com.aiclassroom.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final AiMonitoringService monitoringService;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @PostMapping("/track")
    public ResponseEntity<Void> trackActivity(
            @RequestParam UUID studentId,
            @RequestParam UUID sessionId,
            @RequestParam String activity) {
        User student = userRepository.findById(studentId).orElse(null);
        Session session = sessionRepository.findById(sessionId).orElse(null);
        monitoringService.trackStudentActivity(student, session, activity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/insights/{sessionId}")
    public ResponseEntity<Map<String, Object>> getInsights(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(monitoringService.getClassroomInsights(sessionId));
    }
}
