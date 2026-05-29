package com.aiclassroom.controller;

import com.aiclassroom.entity.Course;
import com.aiclassroom.entity.Session;
import com.aiclassroom.entity.User;
import com.aiclassroom.repository.CourseRepository;
import com.aiclassroom.repository.SessionRepository;
import com.aiclassroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionRepository sessionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * POST /api/sessions
     * Body: { courseId, teacherId, sessionName, startTime (optional ISO string) }
     */
    @PostMapping
    public ResponseEntity<Session> createSession(@RequestBody Map<String, String> body) {
        UUID courseId = UUID.fromString(body.get("courseId"));
        UUID teacherId = UUID.fromString(body.get("teacherId"));
        String sessionName = body.getOrDefault("sessionName", "Live Session");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Session session = new Session();
        session.setCourse(course);
        session.setTeacher(teacher);
        session.setSessionName(sessionName);
        session.setStartTime(LocalDateTime.now());
        session.setIsActive(false);
        session.setParticipantCount(0);

        return ResponseEntity.ok(sessionRepository.save(session));
    }

    /** GET /api/sessions/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable UUID id) {
        return sessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/sessions/course/{courseId} */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Session>> getSessionsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(sessionRepository.findByCourse_Id(courseId));
    }

    /** GET /api/sessions/active */
    @GetMapping("/active")
    public ResponseEntity<List<Session>> getActiveSessions() {
        return ResponseEntity.ok(sessionRepository.findByIsActiveTrue());
    }

    /** PUT /api/sessions/{id}/start */
    @PutMapping("/{id}/start")
    public ResponseEntity<Session> startSession(@PathVariable UUID id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setIsActive(true);
        session.setStartTime(LocalDateTime.now());
        return ResponseEntity.ok(sessionRepository.save(session));
    }

    /** PUT /api/sessions/{id}/end */
    @PutMapping("/{id}/end")
    public ResponseEntity<Session> endSession(@PathVariable UUID id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setIsActive(false);
        session.setEndTime(LocalDateTime.now());
        return ResponseEntity.ok(sessionRepository.save(session));
    }
}
