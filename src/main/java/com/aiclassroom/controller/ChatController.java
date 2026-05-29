package com.aiclassroom.controller;

import com.aiclassroom.entity.Message;
import com.aiclassroom.entity.Session;
import com.aiclassroom.entity.User;
import com.aiclassroom.repository.ChatMessageRepository;
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
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Map<String, String> body) {
        UUID sessionId = UUID.fromString(body.get("sessionId"));
        UUID userId = UUID.fromString(body.get("userId"));
        String content = body.get("content");

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setSession(session);
        message.setSender(user);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setAiResponse(false);

        return ResponseEntity.ok(chatMessageRepository.save(message));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatMessageRepository.findBySession_IdOrderByTimestampAsc(sessionId));
    }
}
