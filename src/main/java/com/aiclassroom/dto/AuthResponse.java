package com.aiclassroom.dto;

import com.aiclassroom.entity.User;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UUID id;
    private String email;
    private String fullName;
    private User.Role role;
    private User.UserStatus status;
}
