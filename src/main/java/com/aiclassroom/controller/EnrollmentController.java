package com.aiclassroom.controller;

import com.aiclassroom.entity.Course;
import com.aiclassroom.entity.Enrollment;
import com.aiclassroom.entity.User;
import com.aiclassroom.repository.CourseRepository;
import com.aiclassroom.repository.EnrollmentRepository;
import com.aiclassroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * POST /api/enrollments
     * Body: { courseId }
     * The student ID is derived from the authenticated JWT principal.
     * Only STUDENT role is permitted to self-enroll.
     */
    @PostMapping
    public ResponseEntity<?> enrollStudent(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, String> body) {

        // Resolve the caller from the security context
        User student = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        // Only students may enroll
        if (student.getRole() != User.Role.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only students can enroll in courses"));
        }

        UUID courseId = UUID.fromString(body.get("courseId"));

        if (enrollmentRepository.existsByStudent_IdAndCourse_Id(student.getId(), courseId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already enrolled in this course"));
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus("ACTIVE");

        return ResponseEntity.ok(enrollmentRepository.save(enrollment));
    }

    /** GET /api/enrollments/student/{studentId} */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudent_Id(studentId));
    }

    /** GET /api/enrollments/course/{courseId} */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(enrollmentRepository.findByCourse_Id(courseId));
    }

    /** DELETE /api/enrollments/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unenroll(@PathVariable UUID id) {
        enrollmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
