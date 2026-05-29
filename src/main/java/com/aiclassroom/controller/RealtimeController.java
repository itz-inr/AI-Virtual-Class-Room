package com.aiclassroom.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class RealtimeController {

    @MessageMapping("/classroom/{sessionId}/chat")
    @SendTo("/topic/classroom/{sessionId}/chat")
    public ChatMessage broadcastChat(@DestinationVariable String sessionId, @Payload ChatMessage message) {
        return message;
    }

    @MessageMapping("/classroom/{sessionId}/state")
    @SendTo("/topic/classroom/{sessionId}/state")
    public ParticipantState broadcastState(@DestinationVariable String sessionId, @Payload ParticipantState state) {
        return state;
    }

    @MessageMapping("/classroom/{sessionId}/reaction")
    @SendTo("/topic/classroom/{sessionId}/reaction")
    public Reaction broadcastReaction(@DestinationVariable String sessionId, @Payload Reaction reaction) {
        return reaction;
    }

    @MessageMapping("/classroom/{sessionId}/monitoring")
    @SendTo("/topic/classroom/{sessionId}/monitoring")
    public Map<String, Object> broadcastMonitoring(@DestinationVariable String sessionId,
            @Payload Map<String, Object> insights) {
        return insights;
    }

    @MessageMapping("/classroom/{sessionId}/signal")
    @SendTo("/topic/classroom/{sessionId}/signal")
    public WebRtcSignal broadcastSignal(@DestinationVariable String sessionId, @Payload WebRtcSignal signal) {
        // Signaling for WebRTC (p2p video/audio)
        return signal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String senderId;
        private String senderName;
        private String content;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantState {
        private String userId;
        private String userName;
        private Boolean isMuted;
        private Boolean isVideoOn;
        private Boolean isHandRaised;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reaction {
        private String userId;
        private String emoji;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRtcSignal {
        private String senderId;
        private String targetId;
        private Object signal; // Offer, Answer, or Candidate
        private String type; // 'offer', 'answer', 'candidate'
    }
}
