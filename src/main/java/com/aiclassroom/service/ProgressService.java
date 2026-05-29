package com.aiclassroom.service;

import com.aiclassroom.entity.Progress;
import com.aiclassroom.entity.User;
import com.aiclassroom.entity.Course;
import com.aiclassroom.repository.ProgressRepository;
import com.aiclassroom.repository.UserRepository;
import com.aiclassroom.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressService {
    
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    
    public List<Progress> getStudentProgress(UUID userId) {
        return progressRepository.findByUser_Id(userId);
    }
    
    public Progress getOrCreateProgress(UUID userId, UUID courseId) {
        return progressRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseGet(() -> {
                    Progress progress = new Progress();
                    User user = userRepository.findById(userId).orElse(null);
                    Course course = courseRepository.findById(courseId).orElse(null);
                    progress.setUser(user);
                    progress.setCourse(course);
                    progress.setCompletion(0.0);
                    progress.setQuizScore(0.0);
                    progress.setTopicsLearned(0);
                    progress.setQuestionsAsked(0);
                    progress.setQuizzesTaken(0);
                    progress.setAttendanceRate(100.0);
                    return progressRepository.save(progress);
                });
    }
    
    public Progress updateProgress(UUID userId, UUID courseId, Double completion, Double quizScore) {
        Progress progress = getOrCreateProgress(userId, courseId);
        if (completion != null) progress.setCompletion(completion);
        if (quizScore != null) progress.setQuizScore(quizScore);
        return progressRepository.save(progress);
    }
    
    public Progress incrementQuestions(UUID userId, UUID courseId) {
        Progress progress = getOrCreateProgress(userId, courseId);
        progress.setQuestionsAsked(progress.getQuestionsAsked() + 1);
        return progressRepository.save(progress);
    }
    
    public Progress incrementQuizzes(UUID userId, UUID courseId, Double score) {
        Progress progress = getOrCreateProgress(userId, courseId);
        progress.setQuizzesTaken(progress.getQuizzesTaken() + 1);
        
        // Calculate average score
        int total = progress.getQuizzesTaken();
        double currentAvg = progress.getQuizScore();
        double newAvg = ((currentAvg * (total - 1)) + score) / total;
        progress.setQuizScore(newAvg);
        
        return progressRepository.save(progress);
    }
    
    public Progress incrementTopics(UUID userId, UUID courseId) {
        Progress progress = getOrCreateProgress(userId, courseId);
        progress.setTopicsLearned(progress.getTopicsLearned() + 1);
        return progressRepository.save(progress);
    }
}
