package com.skynet.auth.dto;

import lombok.Data;

@Data
public class CrearUsuarioRequestDTO {
    // El 'id' no se pide, se genera solo
    private String nombre;
    private String cargo;
    private String correo; // Este será el 'username'
    private String telefono;
    private String password;
    private String rolNombre; // Ej. "ROLE_SUPERVISOR" o "ROLE_TECNICO"

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Opcional, solo se usará si el rolNombre es "ROLE_TECNICO"
    private String supervisorUsername;
}