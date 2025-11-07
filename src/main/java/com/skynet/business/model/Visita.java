package com.skynet.business.model;

import com.skynet.auth.model.Usuario; // ¡Importante!
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import java.time.LocalDateTime; // Usaremos la API moderna de Java para Fechas/Horas

@Data
@NoArgsConstructor
@Entity
@Table(name = "visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relaciones (Foreign Keys) ---



    // Relación: Muchas visitas pueden ser para UN cliente.
    @ManyToOne
    @JoinColumn(name = "cliente_id") // Así se llamará la columna en la BD
    private Cliente cliente;

    // Relación: Muchas visitas pueden ser asignadas a UN técnico.
    @ManyToOne
    @JoinColumn(name = "tecnico_id") // Así se llamará la columna en la BD
    private Usuario tecnico;

    // Relación: Muchas visitas pueden ser planificadas por UN supervisor.
    @ManyToOne
    @JoinColumn(name = "supervisor_id") // Así se llamará la columna en la BD
    private Usuario supervisor;

    // --- Datos de Planificación ---

    private LocalDateTime fechaPlanificada;

    private String estado; // Ej: "PLANIFICADA", "EN_CURSO", "COMPLETADA"

    // --- Datos del Ingreso (Check-in) ---

    private LocalDateTime fechaHoraIngreso;
    private double latitudIngreso;
    private double longitudIngreso;

    // --- Datos del Egreso (Check-out) ---

    private LocalDateTime fechaHoraEgreso;
    private double latitudEgreso;
    private double longitudEgreso;

    // --- Reporte Final ---

    @Column(columnDefinition = "TEXT") // "TEXT" permite reportes largos, a diferencia de VARCHAR(255)
    private String reporteFinal;
}