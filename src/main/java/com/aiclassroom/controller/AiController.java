package com.aiclassroom.controller;

import com.aiclassroom.dto.AiChatRequest;
import com.aiclassroom.dto.AiChatResponse;
import com.aiclassroom.service.AiChatService;
import com.aiclassroom.service.BytezAiService;
import com.aiclassroom.entity.ConceptMasteryProfile;
import com.aiclassroom.entity.Recommendation;
import com.aiclassroom.entity.StudentRiskProfile;
import com.aiclassroom.repository.ConceptMasteryProfileRepository;
import com.aiclassroom.repository.RecommendationRepository;
import com.aiclassroom.repository.StudentRiskProfileRepository;
import com.aiclassroom.service.NextBestTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;
    private final BytezAiService bytezAiService;
    private final NextBestTopicService nextBestTopicService;
    private final ConceptMasteryProfileRepository masteryRepo;
    private final StudentRiskProfileRepository riskRepo;
    private final RecommendationRepository recommendationRepo;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiChatService.chat(request));
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAi(@RequestParam String question) {
        return ResponseEntity.ok(bytezAiService.getAiResponse(question));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @GetMapping("/mastery/{studentId}/{courseId}")
    public ResponseEntity<List<ConceptMasteryProfile>> getMasteryProfiles(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(masteryRepo.findByStudent_IdAndCourse_Id(studentId, courseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @GetMapping("/next-topic/{studentId}/{courseId}")
    public ResponseEntity<String> getNextTopic(
            @PathVariable UUID studentId,
            @PathVariable UUID courseId) {
        return ResponseEntity.ok(nextBestTopicService.getNextBestTopic(studentId, courseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @GetMapping("/risk/{studentId}")
    public ResponseEntity<StudentRiskProfile> getRiskProfile(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(riskRepo.findByStudentId(studentId).orElse(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #studentId == authentication.principal.id")
    @GetMapping("/recommendations/{studentId}")
    public ResponseEntity<List<Recommendation>> getRecommendations(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(recommendationRepo.findByStudent_IdOrderByScoreDesc(studentId));
    }
}
