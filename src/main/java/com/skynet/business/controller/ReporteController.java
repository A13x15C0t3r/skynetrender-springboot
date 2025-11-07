package com.skynet.business.controller;

import com.skynet.business.model.Visita;
import com.skynet.business.repository.VisitaRepository;
import com.skynet.business.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api") // Ruta base (igual que tus otros controladores)
public class ReporteController {

    // --- Inyección de Dependencias por Constructor ---

    private final PdfGenerationService pdfService;
    private final VisitaRepository visitaRepository; // Para buscar la visita

    @Autowired
    public ReporteController(PdfGenerationService pdfService, VisitaRepository visitaRepository) {
        this.pdfService = pdfService;
        this.visitaRepository = visitaRepository;
    }

    /**
     * Endpoint GET para generar un PDF de una visita completada.

     * Protegido por la URL "/api/supervisor/**" (gracias a SecurityConfig),
     * por lo que solo los supervisores y admins (si así lo configuraste)
     * pueden acceder.
     */
    @GetMapping("/supervisor/reportes/visita/{visitaId}")
    public ResponseEntity<InputStreamResource> generarReportePdf(@PathVariable Long visitaId) throws IOException {

        // 1. Buscar la visita en la BD
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada"));

        // 2. Llamar al servicio para generar los bytes del PDF
        byte[] pdfBytes = pdfService.generarPdfVisita(visita);

        // 3. Convertir esos bytes en un InputStream
        ByteArrayInputStream pdfStream = new ByteArrayInputStream(pdfBytes);

        // 4. Preparar las cabeceras (Headers) de la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        String filename = "reporte-visita-" + visitaId + ".pdf";
        headers.add("Content-Disposition", "inline; filename=" + filename);

        // 5. Devolver la respuesta
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}