package com.aiclassroom.controller;

import com.aiclassroom.dto.AuthResponse;
import com.aiclassroom.dto.LoginRequest;
import com.aiclassroom.dto.SignupRequest;
import com.aiclassroom.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.aiclassroom.repository.DepartmentRepository departmentRepository;
    private final com.aiclassroom.repository.AcademicClassRepository academicClassRepository;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/departments")
    public ResponseEntity<java.util.List<com.aiclassroom.entity.Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @GetMapping("/departments/{id}/classes")
    public ResponseEntity<java.util.List<com.aiclassroom.entity.AcademicClass>> getClassesByDepartment(
            @PathVariable java.util.UUID id) {
        return ResponseEntity.ok(academicClassRepository.findByDepartment_Id(id));
    }
}
