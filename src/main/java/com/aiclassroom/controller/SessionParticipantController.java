package com.aiclassroom.controller;

import com.aiclassroom.entity.Session;
import com.aiclassroom.entity.SessionParticipant;
import com.aiclassroom.entity.User;
import com.aiclassroom.repository.EnrollmentRepository;
import com.aiclassroom.repository.SessionParticipantRepository;
import com.aiclassroom.repository.SessionRepository;
import com.aiclassroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/participants")
@RequiredArgsConstructor
public class SessionParticipantController {

    private final SessionParticipantRepository participantRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @PostMapping("/heartbeat")
    public ResponseEntity<SessionParticipant> heartbeat(@RequestBody Map<String, Object> body) {
        Object sId = body.get("sessionId");
        Object uId = body.get("userId");
        if (sId == null || uId == null)
            return ResponseEntity.badRequest().build();

        UUID sessionId = UUID.fromString(sId.toString());
        UUID userId = UUID.fromString(uId.toString());

        if (sessionId == null || userId == null)
            return ResponseEntity.badRequest().build();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ACCESS CONTROL
        User.Role role = user.getRole();
        boolean isHost = User.Role.TEACHER.equals(role) || User.Role.ADMIN.equals(role);

        if (!isHost) {
            // 1. Check if session is active
            if (!Boolean.TRUE.equals(session.getIsActive())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // 2. Check Enrollment
            boolean enrolled = enrollmentRepository.existsByStudent_IdAndCourse_Id(userId, session.getCourse().getId());
            if (!enrolled) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        SessionParticipant participant = participantRepository.findBySession_IdAndUser_Id(sessionId, userId)
                .orElseGet(() -> SessionParticipant.builder()
                        .session(session)
                        .user(user)
                        .isApproved(false)
                        .build());

        // Force approval for hosts
        if (isHost) {
            participant.setIsApproved(true);
        }

        if (body.containsKey("isMuted"))
            participant.setIsMuted((Boolean) body.get("isMuted"));
        if (body.containsKey("isVideoOn"))
            participant.setIsVideoOn((Boolean) body.get("isVideoOn"));
        if (body.containsKey("isHandRaised"))
            participant.setIsHandRaised((Boolean) body.get("isHandRaised"));
        if (body.containsKey("lastReaction"))
            participant.setLastReaction((String) body.get("lastReaction"));

        participant.setLastSeen(LocalDateTime.now());
        participant.setHasLeft(false);

        return ResponseEntity.ok(participantRepository.save(participant));
    }

    @PostMapping("/approve")
    public ResponseEntity<Void> approve(@RequestParam UUID sessionId, @RequestParam UUID participantUserId) {
        participantRepository.findBySession_IdAndUser_Id(sessionId, participantUserId).ifPresent(p -> {
            p.setIsApproved(true);
            participantRepository.save(p);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<SessionParticipant>> getParticipants(@PathVariable UUID sessionId) {
        // In a real app, we'd filter out participants who haven't sent a heartbeat
        // recently
        return ResponseEntity.ok(participantRepository.findBySession_IdAndHasLeftFalse(sessionId));
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leave(@RequestParam UUID sessionId, @RequestParam UUID userId) {
        participantRepository.findBySession_IdAndUser_Id(sessionId, userId).ifPresent(p -> {
            p.setHasLeft(true);
            participantRepository.save(p);
        });
        return ResponseEntity.ok().build();
    }
}
