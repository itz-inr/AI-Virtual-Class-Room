package com.aiclassroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String response;
    private String topic;
    private String mode;
    private Boolean isQuiz;
    private QuizData quizData;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizData {
        private String question;
        private String[] options;
        private Integer correctAnswer;
        private String explanation;
    }
}
