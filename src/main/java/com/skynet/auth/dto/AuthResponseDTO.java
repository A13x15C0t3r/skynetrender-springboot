package com.skynet.auth.dto;

import lombok.Data;

@Data // Lombok: Genera getters y setters
public class AuthResponseDTO {
    private String token;
    private String tipo = "Bearer"; // Estándar de JWT

    public AuthResponseDTO(String token) {
        this.token = token;
    }
}