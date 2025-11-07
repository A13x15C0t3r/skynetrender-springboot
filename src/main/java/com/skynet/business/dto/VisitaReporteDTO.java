package com.skynet.business.dto;

import lombok.Data;

@Data
public class VisitaReporteDTO {
    // Al salir, envía sus coordenadas finales
    private double latitud;
    private double longitud;

    // Y el reporte de lo que hizo
    private String reporteFinal;
}