package com.aiclassroom.controller;

import com.aiclassroom.entity.*;
import com.aiclassroom.repository.*;
import com.aiclassroom.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProgressService progressService;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;
    private final StudentActivityRepository studentActivityRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;

    // ─────────────────────── STUDENT ───────────────────────

    /**
     * GET /api/dashboard/student/{userId}
     * Returns raw progress list (existing behaviour – preserved for api.js).
     */
    @GetMapping("/student/{userId}")
    public ResponseEntity<List<Progress>> getStudentDashboard(@PathVariable UUID userId) {
        return ResponseEntity.ok(progressService.getStudentProgress(userId));
    }

    /**
     * GET /api/dashboard/student/{userId}/stats
     * Returns aggregated stats: coursesCount, gpa, attendanceRate, quizAvg +
     * progress list.
     */
    @GetMapping("/student/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getStudentStats(@PathVariable UUID userId) {
        List<Progress> progressList = progressService.getStudentProgress(userId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_Id(userId);

        int coursesCount = enrollments.size();
        double gpa = 0.0;
        double attendanceRate = 0.0;
        double quizAvg = 0.0;

        if (!progressList.isEmpty()) {
            quizAvg = progressList.stream()
                    .mapToDouble(p -> p.getQuizScore() != null ? p.getQuizScore() : 0)
                    .average().orElse(0);
            attendanceRate = progressList.stream()
                    .mapToDouble(p -> p.getAttendanceRate() != null ? p.getAttendanceRate() : 100)
                    .average().orElse(100);
            // GPA approximation: quizAvg / 25 capped at 4.0
            gpa = Math.min(4.0, quizAvg / 25.0);
        }

        // Build simplified progress list for frontend
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
        List<Map<String, Object>> progress = progressList.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("courseId", p.getCourse().getId());
            m.put("courseName", p.getCourse().getName());
            m.put("completion", p.getCompletion());
            m.put("quizScore", p.getQuizScore());
            m.put("quizzesTaken", p.getQuizzesTaken());
            m.put("topicsLearned", p.getTopicsLearned());
            m.put("questionsAsked", p.getQuestionsAsked());
            m.put("attendanceRate", p.getAttendanceRate());
            if (p.getUpdatedAt() != null)
                m.put("lastActivity", p.getUpdatedAt().format(fmt));
            return m;
        }).collect(Collectors.toList());

        // Upcoming sessions (active sessions of enrolled courses)
        List<Map<String, Object>> upcomingSessions = enrollments.stream()
                .flatMap(e -> sessionRepository.findByCourse_Id(e.getCourse().getId()).stream())
                .filter(s -> !Boolean.TRUE.equals(s.getIsActive())) // not yet live
                .sorted(Comparator.comparing(Session::getStartTime))
                .limit(5)
                .map(s -> {
                    Map<String, Object> sm = new LinkedHashMap<>();
                    sm.put("sessionId", s.getId());
                    sm.put("sessionName", s.getSessionName());
                    sm.put("courseName", s.getCourse().getName());
                    sm.put("startTime", s.getStartTime().format(DateTimeFormatter.ofPattern("MMM d, h:mm a")));
                    sm.put("isActive", s.getIsActive());
                    return sm;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("coursesCount", coursesCount);
        result.put("gpa", Math.round(gpa * 10.0) / 10.0);
        result.put("attendanceRate", Math.round(attendanceRate));
        result.put("quizAvg", Math.round(quizAvg));
        result.put("progress", progress);
        result.put("upcomingSessions", upcomingSessions);

        return ResponseEntity.ok(result);
    }

    // ─────────────────────── TEACHER ───────────────────────

    /**
     * GET /api/dashboard/teacher/{teacherId}/stats
     * Returns: activeCourses, totalStudents, avgPerformance, sessionsToday +
     * courses list + recentActivity.
     */
    @GetMapping("/teacher/{teacherId}/stats")
    public ResponseEntity<Map<String, Object>> getTeacherStats(@PathVariable UUID teacherId) {
        List<Course> courses = courseRepository.findByTeacher_Id(teacherId);

        // Student count across all teacher courses via enrollments
        Set<UUID> uniqueStudents = new HashSet<>();
        courses.forEach(c -> enrollmentRepository.findByCourse_Id(c.getId())
                .forEach(e -> uniqueStudents.add(e.getStudent().getId())));

        // Avg performance = avg of quizScore across all progress records for teacher's
        // courses
        double avgPerformance = 0.0;
        List<Progress> allProgress = courses.stream()
                .flatMap(c -> progressRepository.findByCourse_Id(c.getId()).stream())
                .collect(Collectors.toList());
        if (!allProgress.isEmpty()) {
            avgPerformance = allProgress.stream()
                    .mapToDouble(p -> p.getQuizScore() != null ? p.getQuizScore() : 0)
                    .average().orElse(0);
        }

        // Sessions today
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long sessionsToday = courses.stream()
                .flatMap(c -> sessionRepository.findByCourse_Id(c.getId()).stream())
                .filter(s -> s.getStartTime() != null
                        && !s.getStartTime().isBefore(startOfDay)
                        && s.getStartTime().isBefore(endOfDay))
                .count();

        // Enrich courses with real enrollment count
        List<Map<String, Object>> courseList = courses.stream().map(c -> {
            int enrolled = enrollmentRepository.findByCourse_Id(c.getId()).size();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("description", c.getDescription());
            m.put("aiMode", c.getAiMode() != null ? c.getAiMode().name() : "DISABLED");
            m.put("studentCount", enrolled);
            m.put("active", c.getActive());
            return m;
        }).collect(Collectors.toList());

        // Recent student activity: latest progress records across teacher's courses
        // (sorted by updatedAt)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, h:mm a");
        List<Map<String, Object>> recentActivity = allProgress.stream()
                .filter(p -> p.getUpdatedAt() != null)
                .sorted(Comparator.comparing(Progress::getUpdatedAt).reversed())
                .limit(5)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("studentName", p.getUser().getFullName());
                    m.put("courseName", p.getCourse().getName());
                    m.put("quizScore", p.getQuizScore());
                    m.put("quizzesTaken", p.getQuizzesTaken());
                    m.put("completion", p.getCompletion());
                    m.put("lastActivity", p.getUpdatedAt().format(fmt));
                    return m;
                }).collect(Collectors.toList());

        // Upcoming sessions (for this teacher)
        List<Map<String, Object>> upcomingSessions = courses.stream()
                .flatMap(c -> sessionRepository.findByCourse_Id(c.getId()).stream())
                .sorted(Comparator.comparing(Session::getStartTime))
                .limit(10)
                .map(s -> {
                    int enrolled = enrollmentRepository.findByCourse_Id(s.getCourse().getId()).size();
                    Map<String, Object> sm = new LinkedHashMap<>();
                    sm.put("sessionId", s.getId());
                    sm.put("sessionName", s.getSessionName());
                    sm.put("courseName", s.getCourse().getName());
                    sm.put("startTime", s.getStartTime().format(fmt));
                    sm.put("isActive", s.getIsActive());
                    sm.put("studentCount", enrolled);
                    return sm;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeCourses", courses.size());
        result.put("totalStudents", uniqueStudents.size());
        result.put("avgPerformance", Math.round(avgPerformance));
        result.put("sessionsToday", sessionsToday);
        result.put("courses", courseList);
        result.put("recentActivity", recentActivity);
        result.put("upcomingSessions", upcomingSessions);

        return ResponseEntity.ok(result);
    }

    // ─────────────────────── ADMIN ───────────────────────

    /**
     * GET /api/dashboard/admin/stats
     * Returns: totalUsers, totalTeachers, totalStudents, activeCourses.
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalTeachers = userRepository.findByRole(User.Role.TEACHER).size();
        long totalStudents = userRepository.findByRole(User.Role.STUDENT).size();
        long activeCourses = courseRepository.findByActiveTrue().size();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalUsers", totalUsers);
        result.put("totalTeachers", totalTeachers);
        result.put("totalStudents", totalStudents);
        result.put("activeCourses", activeCourses);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────── PROGRESS MUTATIONS ───────────────────────

    @PostMapping("/progress")
    public ResponseEntity<Progress> updateProgress(
            @RequestParam UUID userId,
            @RequestParam UUID courseId,
            @RequestParam(required = false) Double completion,
            @RequestParam(required = false) Double quizScore) {
        return ResponseEntity.ok(progressService.updateProgress(userId, courseId, completion, quizScore));
    }

    @PostMapping("/progress/question")
    public ResponseEntity<Progress> incrementQuestion(
            @RequestParam UUID userId,
            @RequestParam UUID courseId) {
        return ResponseEntity.ok(progressService.incrementQuestions(userId, courseId));
    }

    @PostMapping("/progress/quiz")
    public ResponseEntity<Progress> incrementQuiz(
            @RequestParam UUID userId,
            @RequestParam UUID courseId,
            @RequestParam Double score) {
        return ResponseEntity.ok(progressService.incrementQuizzes(userId, courseId, score));
    }

    @PostMapping("/progress/topic")
    public ResponseEntity<Progress> incrementTopic(
            @RequestParam UUID userId,
            @RequestParam UUID courseId) {
        return ResponseEntity.ok(progressService.incrementTopics(userId, courseId));
    }
}
