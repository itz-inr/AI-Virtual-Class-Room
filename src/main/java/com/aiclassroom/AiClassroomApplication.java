package com.aiclassroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.aiclassroom.repository")
public class AiClassroomApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiClassroomApplication.class, args);
        System.out.println("=".repeat(60));
        System.out.println("🚀 AI Classroom Platform Started Successfully!");
        System.out.println("📍 Running on: http://localhost:8080");
        System.out.println("=".repeat(60));
    }
}
