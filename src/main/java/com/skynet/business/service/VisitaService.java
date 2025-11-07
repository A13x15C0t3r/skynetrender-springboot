package com.skynet.business.service;

// --- Imports de Autenticación ---
import com.skynet.auth.model.Usuario;
import com.skynet.auth.repository.UsuarioRepository;

// --- Imports de Negocio (DTOs y Modelos) ---
import com.skynet.business.dto.PlanificacionRequestDTO;
import com.skynet.business.dto.VisitaGeopuntoDTO;
import com.skynet.business.dto.VisitaReporteDTO;
import com.skynet.business.model.Cliente;
import com.skynet.business.model.Visita;

// --- Imports de Negocio (Repositorios) ---
import com.skynet.business.repository.ClienteRepository;
import com.skynet.business.repository.VisitaRepository;

// --- Imports de Spring y Java ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio principal que maneja toda la lógica de negocio
 * relacionada con las Visitas.
 */
@Service
public class VisitaService {

    // --- Inyección de Dependencias ---
    // (Declaradas como 'final' para ser inicializadas en el constructor)
    private final VisitaRepository visitaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PdfGenerationService pdfService;

    /**
     * Inyección por Constructor (la mejor práctica de Spring)
     * Spring inyectará automáticamente las instancias de estos servicios/repositorios.
     */
    @Autowired
    public VisitaService(VisitaRepository visitaRepository,
                         ClienteRepository clienteRepository,
                         UsuarioRepository usuarioRepository,
                         EmailService emailService,
                         PdfGenerationService pdfService) {
        this.visitaRepository = visitaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.pdfService = pdfService;
    }

    /**
     * Lógica de negocio para planificar una visita (usada por Supervisor/Admin).
     */
    @Transactional
    public Visita planificarVisita(PlanificacionRequestDTO dto, String supervisorUsername) {

        // 1. Buscar las entidades en la BD
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId()));

        Usuario tecnico = usuarioRepository.findByUsername(dto.getTecnicoUsername())
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado con username: " + dto.getTecnicoUsername()));

        Usuario supervisor = usuarioRepository.findByUsername(supervisorUsername)
                .orElseThrow(() -> new RuntimeException("Supervisor no encontrado"));

        // 2. Crear la nueva entidad Visita
        Visita nuevaVisita = new Visita();
        nuevaVisita.setCliente(cliente);
        nuevaVisita.setTecnico(tecnico);
        nuevaVisita.setSupervisor(supervisor); // Guarda quién la planificó
        nuevaVisita.setFechaPlanificada(dto.getFechaPlanificada());
        nuevaVisita.setEstado("PLANIFICADA");

        // 3. Guardar en la base de datos
        return visitaRepository.save(nuevaVisita);
    }

    /**
     * Lógica de negocio para el CHECK-IN de un técnico.
     */
    @Transactional
    public Visita registrarIngreso(Long visitaId, VisitaGeopuntoDTO dto, String tecnicoUsername) {

        // 1. Buscar la visita
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada con ID: " + visitaId));

        // 2. Validación de Seguridad: ¿El técnico es el dueño de esta visita?
        if (!visita.getTecnico().getUsername().equals(tecnicoUsername)) {
            throw new SecurityException("Acción no autorizada: Este técnico no está asignado a esta visita.");
        }

        // 3. Validación de Estado: ¿La visita está 'PLANIFICADA'?
        if (!"PLANIFICADA".equals(visita.getEstado())) {
            throw new IllegalStateException("Acción no válida: La visita no está en estado 'PLANIFICADA'. Estado actual: " + visita.getEstado());
        }

        // 4. Actualizar la entidad
        visita.setEstado("EN_CURSO");
        visita.setFechaHoraIngreso(LocalDateTime.now());
        visita.setLatitudIngreso(dto.getLatitud());
        visita.setLongitudIngreso(dto.getLongitud());

        // 5. Guardar
        return visitaRepository.save(visita);
    }

    /**
     * Lógica de negocio para el CHECK-OUT de un técnico.
     * Esto también genera y envía el PDF por correo.
     */
    @Transactional
    public Visita registrarEgreso(Long visitaId, VisitaReporteDTO dto, String tecnicoUsername) {

        // 1. Buscar la visita
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada con ID: " + visitaId));

        // 2. Validación de Seguridad: ¿Es el técnico correcto?
        if (!visita.getTecnico().getUsername().equals(tecnicoUsername)) {
            throw new SecurityException("Acción no autorizada: Este técnico no está asignado a esta visita.");
        }

        // 3. Validación de Estado: ¿La visita está 'EN_CURSO'?
        if (!"EN_CURSO".equals(visita.getEstado())) {
            throw new IllegalStateException("Acción no válida: La visita no está en estado 'EN_CURSO'. Estado actual: " + visita.getEstado());
        }

        // 4. Actualizar la entidad
        visita.setEstado("COMPLETADA");
        visita.setFechaHoraEgreso(LocalDateTime.now());
        visita.setLatitudEgreso(dto.getLatitud());
        visita.setLongitudEgreso(dto.getLongitud());
        visita.setReporteFinal(dto.getReporteFinal());

        // 5. Guardar los cambios
        Visita visitaCompletada = visitaRepository.save(visita);

        // 6. Generar y Enviar PDF por Email (al Supervisor y al Cliente)
        try {
            byte[] pdfBytes = pdfService.generarPdfVisita(visitaCompletada);
            emailService.enviarReporteVisitaConAdjunto(visitaCompletada, pdfBytes);
        } catch (IOException e) {
            // Si el PDF falla, al menos registra el error (pero no detiene la transacción)
            System.err.println(">>> Error al generar el PDF para el email: " + e.getMessage());
        }

        return visitaCompletada;
    }

    /**
     * Lógica para el Dashboard del Supervisor (Mapa y Tabla de Activas).
     * Obtiene las visitas activas del EQUIPO del supervisor.
     */
    public List<Visita> obtenerDashboardSupervisor(String supervisorUsername) {
        Usuario supervisor = usuarioRepository.findByUsername(supervisorUsername)
                .orElseThrow(() -> new RuntimeException("Supervisor no encontrado: " + supervisorUsername));

        // Usa la consulta anidada: visita -> tecnico -> supervisorAsignado
        return visitaRepository.findByTecnico_SupervisorAsignadoAndEstadoNot(supervisor, "COMPLETADA");
    }

    /**
     * Lógica para el Dashboard del Técnico.
     * Obtiene las visitas activas (no completadas) asignadas a él.
     */
    public List<Visita> obtenerDashboardTecnico(String tecnicoUsername) {
        Usuario tecnico = usuarioRepository.findByUsername(tecnicoUsername)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado: " + tecnicoUsername));

        return visitaRepository.findByTecnicoAndEstadoNot(tecnico, "COMPLETADA");
    }

    /**
     * Lógica para el Historial del Supervisor (Tabla de Completadas).
     * Obtiene el historial de visitas del EQUIPO del supervisor.
     */
    public List<Visita> obtenerHistorialSupervisor(String supervisorUsername) {
        Usuario supervisor = usuarioRepository.findByUsername(supervisorUsername)
                .orElseThrow(() -> new RuntimeException("Supervisor no encontrado: " + supervisorUsername));

        // Usa la consulta anidada: visita -> tecnico -> supervisorAsignado
        return visitaRepository.findByTecnico_SupervisorAsignadoAndEstado(supervisor, "COMPLETADA");
    }

    /**
     * Obtiene una lista de técnicos disponibles para asignar.
     * Si es Admin, ve a TODOS. Si es Supervisor, ve solo a SU EQUIPO.
     */
    public List<Usuario> obtenerTecnicosDisponibles(String usernameLogueado) {

        Usuario usuarioLogueado = usuarioRepository.findByUsername(usernameLogueado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usernameLogueado));

        // Verificamos si el usuario es ADMIN
        boolean esAdmin = usuarioLogueado.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));

        if (esAdmin) {
            // El ADMIN ve a TODOS los técnicos
            return usuarioRepository.findByRoles_Nombre("ROLE_TECNICO");
        } else {
            // El SUPERVISOR ve solo a SU EQUIPO
            return usuarioRepository.findByRoles_NombreAndSupervisorAsignado("ROLE_TECNICO", usuarioLogueado);
        }
    }
}