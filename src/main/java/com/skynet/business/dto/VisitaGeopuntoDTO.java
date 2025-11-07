package com.skynet.business.dto;

import lombok.Data;

@Data
public class VisitaGeopuntoDTO {
    // El técnico solo envía sus coordenadas actuales
    private double latitud;
    private double longitud;
}