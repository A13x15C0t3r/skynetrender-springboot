package com.skynet.business.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlanificacionRequestDTO {
    private Long clienteId;       // El ID del cliente a visitar
    private String tecnicoUsername; // El username del técnico asignado
    private LocalDateTime fechaPlanificada; // Cuándo debe ser la visita
}