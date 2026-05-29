package com.aiclassroom.controller;

import com.aiclassroom.dto.SignupRequest;
import com.aiclassroom.entity.User;
import com.aiclassroom.entity.User.UserStatus;
import com.aiclassroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.Statement;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.aiclassroom.repository.DepartmentRepository departmentRepository;
    private final com.aiclassroom.repository.AcademicClassRepository academicClassRepository;
    private final com.aiclassroom.repository.CourseRepository courseRepository;
    private final com.aiclassroom.repository.EnrollmentRepository enrollmentRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void repairDatabase() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            // Force add columns if missing
            stmt.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS class_id UUID;");
            stmt.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS subject_id UUID;");
            System.out.println("Database columns verified/added.");
        } catch (Exception e) {
            System.err.println("Database repair failed: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        User user = userRepository.findById(id).orElseThrow();
        String status = body.get("status");
        if (status != null) {
            user.setStatus(User.UserStatus.valueOf(status));
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/users/{id}/academic")
    public ResponseEntity<User> updateUserAcademic(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        User user = userRepository.findById(id).orElseThrow();

        String deptId = body.get("departmentId");
        String classId = body.get("classId");

        if (deptId != null && !deptId.isEmpty()) {
            user.setDepartment(departmentRepository.findById(UUID.fromString(deptId)).orElse(null));
        } else if (body.containsKey("departmentId") && deptId == null) {
            user.setDepartment(null);
        }

        if (classId != null && !classId.isEmpty()) {
            user.setAcademicClass(academicClassRepository.findById(UUID.fromString(classId)).orElse(null));
        } else if (body.containsKey("classId") && classId == null) {
            user.setAcademicClass(null);
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (id == null)
            return ResponseEntity.badRequest().build();

        // Manual cleanup to handle FK constraints safely
        List<com.aiclassroom.entity.Course> courses = courseRepository.findByTeacher_Id(id);
        for (com.aiclassroom.entity.Course c : courses) {
            // Delete all enrollments in these courses first
            enrollmentRepository.deleteAll(enrollmentRepository.findByCourse_Id(c.getId()));
        }
        courseRepository.deleteAll(courses);

        // Delete user's own enrollments (if they were a student)
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudent_Id(id));

        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Admin creates a user directly with ACTIVE status (bypassing public signup
     * PENDING flow).
     */
    @PostMapping("/users")
    public ResponseEntity<User> adminCreateUser(@RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        if (request.getDepartmentId() != null) {
            user.setDepartment(departmentRepository.findById(request.getDepartmentId()).orElse(null));
        }
        if (request.getClassId() != null) {
            user.setAcademicClass(academicClassRepository.findById(request.getClassId()).orElse(null));
        }

        // Admin-created users are always ACTIVE
        user.setStatus(UserStatus.ACTIVE);

        return ResponseEntity.ok(userRepository.save(user));
    }
}
