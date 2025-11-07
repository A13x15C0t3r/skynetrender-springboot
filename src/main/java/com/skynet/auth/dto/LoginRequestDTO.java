package com.skynet.auth.dto;

import lombok.Data;

@Data // Lombok: Genera getters y setters
public class LoginRequestDTO {
    private String username;
    private String password;
}