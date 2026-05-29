package com.aiclassroom.service;

import com.aiclassroom.dto.AuthResponse;
import com.aiclassroom.dto.LoginRequest;
import com.aiclassroom.dto.SignupRequest;
import com.aiclassroom.entity.User;
import com.aiclassroom.entity.User.UserStatus;
import com.aiclassroom.repository.UserRepository;
import com.aiclassroom.repository.AcademicClassRepository;
import com.aiclassroom.repository.DepartmentRepository;
import com.aiclassroom.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicClassRepository academicClassRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(request.getRole() == User.Role.ADMIN ? UserStatus.ACTIVE : UserStatus.PENDING);

        java.util.UUID deptId = request.getDepartmentId();
        if (deptId != null) {
            user.setDepartment(departmentRepository.findById(deptId).orElse(null));
        }
        java.util.UUID clsId = request.getClassId();
        if (clsId != null) {
            user.setAcademicClass(academicClassRepository.findById(clsId).orElse(null));
        }

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new RuntimeException("Your account is pending admin approval. Please contact your administrator.");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new RuntimeException("Your account has been rejected. Please contact your administrator.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
