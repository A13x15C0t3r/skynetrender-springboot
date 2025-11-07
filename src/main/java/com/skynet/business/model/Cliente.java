package com.skynet.business.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreEmpresa;

    private String personaContacto;
    private String telefono;

    // --- ¡CAMPO NUEVO! ---
    @Column(unique = true) // Opcional, pero recomendado
    private String correo; // Ej. "contacto@empresa.com"

    // --- CAMPOS EXISTENTES ---
    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;
}