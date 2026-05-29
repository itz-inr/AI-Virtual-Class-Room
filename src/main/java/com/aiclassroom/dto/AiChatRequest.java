package com.aiclassroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String topic;

    private String mode; // EXPLAIN, QUIZ, REVISION

    private java.util.UUID studentId;

    private java.util.UUID courseId;

    private Boolean isCorrect; // Optional flag, set to true if answering a previous quiz question correctly
}
