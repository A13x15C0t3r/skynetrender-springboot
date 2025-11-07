package com.skynet.business.controller;

// --- Imports de DTOs ---
import com.skynet.business.dto.PlanificacionRequestDTO;
import com.skynet.business.dto.VisitaGeopuntoDTO;
import com.skynet.business.dto.VisitaReporteDTO;

// --- Imports de Modelos y Servicios ---
import com.skynet.auth.model.Usuario; // Import para el método de Técnicos
import com.skynet.business.model.Visita;
import com.skynet.business.service.VisitaService;

// --- Imports de Spring y Java ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*; // Asegura que @GetMapping esté
import java.util.List;

@RestController
@RequestMapping("/api") // Ruta base
public class VisitaController {

    private final VisitaService visitaService;

    @Autowired
    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    /**
     * Endpoint para que un SUPERVISOR planifique una visita.
     */
    @PostMapping("/supervisor/visitas/planificar")
    public ResponseEntity<Visita> planificarVisita(
            @RequestBody PlanificacionRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String supervisorUsername = userDetails.getUsername();
        Visita visitaCreada = visitaService.planificarVisita(dto, supervisorUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitaCreada);
    }

    /**
     * Endpoint para que un TÉCNICO registre su INGRESO (Check-in).
     */
    @PostMapping("/tecnico/visitas/ingreso/{visitaId}")
    public ResponseEntity<Visita> registrarIngreso(
            @PathVariable Long visitaId,
            @RequestBody VisitaGeopuntoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String tecnicoUsername = userDetails.getUsername();
        Visita visitaActualizada = visitaService.registrarIngreso(visitaId, dto, tecnicoUsername);
        return ResponseEntity.ok(visitaActualizada);
    }

    /**
     * Endpoint para que un TÉCNICO registre su EGRESO (Check-out).
     */
    @PostMapping("/tecnico/visitas/egreso/{visitaId}")
    public ResponseEntity<Visita> registrarEgreso(
            @PathVariable Long visitaId,
            @RequestBody VisitaReporteDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String tecnicoUsername = userDetails.getUsername();
        Visita visitaCompletada = visitaService.registrarEgreso(visitaId, dto, tecnicoUsername);
        return ResponseEntity.ok(visitaCompletada);
    }

    /**
     * Endpoint GET para el DASHBOARD DEL SUPERVISOR (Mapa y Tabla Activa).
     */
    @GetMapping("/supervisor/dashboard")
    public ResponseEntity<List<Visita>> getDashboardSupervisor(
            @AuthenticationPrincipal UserDetails userDetails) {

        String supervisorUsername = userDetails.getUsername();
        List<Visita> visitas = visitaService.obtenerDashboardSupervisor(supervisorUsername);
        return ResponseEntity.ok(visitas);
    }

    /**
     * Endpoint GET para el DASHBOARD DEL TÉCNICO.
     */
    @GetMapping("/tecnico/dashboard")
    public ResponseEntity<List<Visita>> getDashboardTecnico(
            @AuthenticationPrincipal UserDetails userDetails) {

        String tecnicoUsername = userDetails.getUsername();
        List<Visita> visitas = visitaService.obtenerDashboardTecnico(tecnicoUsername);
        return ResponseEntity.ok(visitas);
    }

    /**
     * Endpoint GET para la lista de Técnicos (para el formulario de planificar).
     */
    @GetMapping("/supervisor/tecnicos")
    public ResponseEntity<List<Usuario>> getTodosLosTecnicos(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Usuario> tecnicos = visitaService.obtenerTecnicosDisponibles(userDetails.getUsername());
        return ResponseEntity.ok(tecnicos);
    }

    // --- ¡ESTE ES EL MÉTODO QUE TE FALTABA! ---

    /**
     * Endpoint GET para el HISTORIAL de visitas completadas del Supervisor.
     * (Este es el que te daba 404).
     */
    @GetMapping("/supervisor/historial")
    public ResponseEntity<List<Visita>> getHistorialSupervisor(
            @AuthenticationPrincipal UserDetails userDetails) {

        String supervisorUsername = userDetails.getUsername();
        List<Visita> visitas = visitaService.obtenerHistorialSupervisor(supervisorUsername);
        return ResponseEntity.ok(visitas);
    }
}