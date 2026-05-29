package com.aiclassroom.service;

import com.aiclassroom.dto.AiChatRequest;
import com.aiclassroom.dto.AiChatResponse;
import com.aiclassroom.entity.ConceptMasteryProfile;
import com.aiclassroom.entity.Course;
import com.aiclassroom.entity.DifficultyLevel;
import com.aiclassroom.entity.User;
import com.aiclassroom.repository.ConceptMasteryProfileRepository;
import com.aiclassroom.repository.CourseRepository;
import com.aiclassroom.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final BytezAiService bytezAiService;
    private final ObjectMapper objectMapper;
    private final ConceptMasteryProfileRepository masteryRepo;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AiChatResponse chat(AiChatRequest request) {
        String topic = request.getTopic() != null ? request.getTopic() : "General";
        String mode = request.getMode() != null ? request.getMode() : "EXPLAIN";
        String message = request.getMessage();

        // 1. Get User
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        User student = userRepository.findByEmail(email).orElse(null);

        // 2. Locate or Initialize ConceptMasteryProfile
        ConceptMasteryProfile masteryProfile = null;
        UUID courseId = request.getCourseId();
        if (student != null && courseId != null && !topic.equals("General")) {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                masteryProfile = masteryRepo.findByStudent_IdAndCourse_IdAndTopic(
                        student.getId(), course.getId(), topic).orElseGet(() -> {
                            ConceptMasteryProfile p = new ConceptMasteryProfile();
                            p.setStudent(student);
                            p.setCourse(course);
                            p.setTopic(topic);
                            p.setMasteryScore(0);
                            p.setAttemptCount(0);
                            p.setCorrectCount(0);
                            p.setDifficultyLevel(DifficultyLevel.BEGINNER);
                            return p;
                        });

                if (masteryProfile.getId() == null) {
                    masteryProfile = masteryRepo.save(masteryProfile);
                }
            }
        }

        // Apply dynamic difficulty
        String difficultyPrefix = "";
        if (masteryProfile != null) {
            int score = masteryProfile.getMasteryScore();
            if (score < 40) {
                difficultyPrefix = "(BEGINNER: Please explain using very simple terms and basic examples.) ";
                masteryProfile.setDifficultyLevel(DifficultyLevel.BEGINNER);
            } else if (score <= 70) {
                difficultyPrefix = "(INTERMEDIATE: Provide a balanced explanation with practical examples.) ";
                masteryProfile.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
            } else {
                difficultyPrefix = "(ADVANCED: Provide an in-depth, complex explanation assuming prior knowledge.) ";
                masteryProfile.setDifficultyLevel(DifficultyLevel.ADVANCED);
            }
        }

        // Process answer check if client sent "isCorrect" for previous Quiz
        if (masteryProfile != null && request.getIsCorrect() != null) {
            if (request.getIsCorrect()) {
                masteryProfile.setMasteryScore(Math.min(100, masteryProfile.getMasteryScore() + 5));
                masteryProfile.setCorrectCount(masteryProfile.getCorrectCount() + 1);
            } else {
                masteryProfile.setMasteryScore(Math.max(0, masteryProfile.getMasteryScore() - 3));
            }
            masteryRepo.save(masteryProfile);
        }

        // Build a structured prompt based on the mode
        String baseScope = "IMPORTANT: You are a strict academic tutor for the topic '" + topic + "'. "
                + difficultyPrefix
                + "You MUST ONLY answer questions related to " + topic + ". "
                + "If the user asks anything outside this topic, reply with: 'I am here to help you with " + topic
                + " specifically. Please ask questions related to this subject alone.' "
                + "Do not engage in off-topic conversation.";

        String structuredPrompt;
        switch (mode) {
            case "EXPLAIN" -> {
                structuredPrompt = baseScope + "\n\nExplain the following (fresh perspective, use a new angle): "
                        + message;
                if (masteryProfile != null) {
                    masteryProfile.setAttemptCount(masteryProfile.getAttemptCount() + 1);
                    masteryProfile.setLastAttemptedAt(java.time.LocalDateTime.now());
                    masteryRepo.save(masteryProfile);
                }
            }
            case "QUIZ" -> {
                int seed = new java.util.Random().nextInt(10000);
                String[] questionStyles = {
                        "Ask about a core definition.",
                        "Ask about a common mistake beginners make.",
                        "Ask about a practical real-world application.",
                        "Ask about how two concepts compare or differ.",
                        "Ask about what happens when a specific rule is broken."
                };
                String style = questionStyles[seed % questionStyles.length];
                structuredPrompt = baseScope
                        + "\n\nYou are a strict JSON API. Generate exactly ONE unique multiple-choice quiz question (seed: "
                        + seed + ") focused entirely on the academic subject/topic: '"
                        + topic + "'."
                        + "\nQuestion style hint: " + style
                        + "\nThe student asked/stated: '" + message
                        + "'. You MUST create a different question each time about '" + topic + "'."
                        + "\n\nCRITICAL INSTRUCTIONS:"
                        + "\n1. Respond with a RAW, VALID JSON OBJECT ONLY."
                        + "\n2. DO NOT use markdown code blocks (no ```json)."
                        + "\n3. DO NOT include any conversational filler, greetings, or explanations outside the JSON."
                        + "\n4. The highest 'correctAnswer' index is 3 (0-indexed)."
                        + "\n5. Make sure the question is DIFFERENT from the previous questions you have answered."
                        + "\nRequired JSON Structure:"
                        + "\n{\"question\": \"...\", \"options\": [\"Option 1\",\"Option 2\",\"Option 3\",\"Option 4\"], \"correctAnswer\": 0, \"explanation\": \"...\"}";
                // Don't update attemptCount/mastery for generating a quiz, wait for the actual
                // answer.
            }
            case "REVISION" -> {
                structuredPrompt = baseScope + "\n\nGive a concise revision summary covering: " + message
                        + ". Use bullet points and highlight key facts.";
                if (masteryProfile != null) {
                    masteryProfile.setAttemptCount(masteryProfile.getAttemptCount() + 1);
                    masteryProfile.setLastAttemptedAt(java.time.LocalDateTime.now());
                    masteryRepo.save(masteryProfile);
                }
            }
            default -> structuredPrompt = baseScope + "\n\nUser message: " + message;
        }

        String aiResponse = bytezAiService.getAiResponse(structuredPrompt);

        // For QUIZ mode, try to parse the response as JSON
        if ("QUIZ".equals(mode)) {
            try {
                String raw = aiResponse.trim();

                // Step 1: Strip markdown code fences (```json ... ``` or ``` ... ```)
                raw = raw.replaceAll("(?s)```[a-zA-Z]*\\n?(.*?)```", "$1").trim();

                // Step 2: Extract outermost JSON object
                // Walk from first '{' counting depth to find the matching '}'
                int start = raw.indexOf('{');
                String json = null;
                if (start != -1) {
                    int depth = 0;
                    boolean inString = false;
                    boolean escaped = false;
                    for (int i = start; i < raw.length(); i++) {
                        char c = raw.charAt(i);
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        if (c == '\\') {
                            escaped = true;
                            continue;
                        }
                        if (c == '"') {
                            inString = !inString;
                            continue;
                        }
                        if (!inString) {
                            if (c == '{')
                                depth++;
                            else if (c == '}') {
                                depth--;
                                if (depth == 0) {
                                    json = raw.substring(start, i + 1);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (json != null) {
                    // Step 3: Replace actual (unescaped) newlines inside JSON string values
                    // This handles code snippets where Groq puts real \n instead of \\n
                    json = fixJsonNewlines(json);

                    AiChatResponse.QuizData quizData = objectMapper.readValue(json, AiChatResponse.QuizData.class);
                    return AiChatResponse.builder()
                            .response(aiResponse)
                            .topic(topic)
                            .mode(mode)
                            .isQuiz(true)
                            .quizData(quizData)
                            .build();
                }
            } catch (Exception e) {
                System.err.println("[Quiz] JSON parsing failed: " + e.getMessage());
                // Fall through to plain text response
            }
        }

        return AiChatResponse.builder()
                .response(aiResponse)
                .topic(topic)
                .mode(mode)
                .isQuiz(false)
                .build();
    }

    /**
     * Replace unescaped newlines and tabs that appear inside JSON string values.
     * Groq sometimes puts real \n characters inside option strings (e.g. code
     * snippets).
     */
    private String fixJsonNewlines(String json) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                sb.append(c);
                continue;
            }
            if (c == '"') {
                inString = !inString;
                sb.append(c);
                continue;
            }
            if (inString) {
                // Replace actual newline/tab/carriage-return with safe escaped versions
                if (c == '\n') {
                    sb.append("\\n");
                    continue;
                }
                if (c == '\r') {
                    sb.append("\\r");
                    continue;
                }
                if (c == '\t') {
                    sb.append("\\t");
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
