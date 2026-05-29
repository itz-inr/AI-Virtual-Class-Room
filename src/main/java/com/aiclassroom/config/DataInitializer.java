package com.aiclassroom.config;

import com.aiclassroom.entity.User;
import com.aiclassroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@classroom.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@classroom.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFullName("Admin User");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(admin);
            System.out.println("✓ Admin user created: admin@classroom.com / admin123");
        }
        
        if (userRepository.findByEmail("teacher@classroom.com").isEmpty()) {
            User teacher = new User();
            teacher.setEmail("teacher@classroom.com");
            teacher.setPasswordHash(passwordEncoder.encode("teacher123"));
            teacher.setFullName("Demo Teacher");
            teacher.setRole(User.Role.TEACHER);
            teacher.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(teacher);
            System.out.println("✓ Teacher user created: teacher@classroom.com / teacher123");
        }
    }
}
