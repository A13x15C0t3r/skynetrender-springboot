package com.skynet.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <-- ¡IMPORT NECESARIO!
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- CAMPOS NUEVOS ---
    @Column(nullable = false)
    private String nombre; // Ej. "Juan Pérez"

    private String cargo; // Ej. "Supervisor de Zona"

    private String telefono; // Ej. "555-1234"

    // --- CAMPOS EXISTENTES ---

    // 'username' se usará para el 'correo'
    @Column(unique = true, nullable = false)
    private String username; // Ej. "juan.perez@skynet.com"

    @Column(nullable = false)
    private String password; // Encriptada

    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles;


    // --- ¡AQUÍ ESTÁ TU CAMBIO RECOMENDADO! ---

    /**
     * Relación "Muchos-a-Uno" auto-referenciada.
     * Muchos técnicos (Usuarios) reportan a UN supervisor (Usuario).
     */
    @ManyToOne(fetch = FetchType.LAZY) // LAZY = No cargues al supervisor a menos que se pida
    @JoinColumn(name = "supervisor_asignado_id") // El nombre de la nueva columna (Foreign Key)
    @JsonIgnoreProperties({"supervisorAsignado", "roles", "hibernateLazyInitializer"}) // Evita bucles infinitos al convertir a JSON
    private Usuario supervisorAsignado;

}