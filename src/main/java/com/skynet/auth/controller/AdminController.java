package com.skynet.auth.controller;

import com.skynet.auth.dto.CrearUsuarioRequestDTO;
import com.skynet.auth.model.Usuario;
import com.skynet.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping; // <-- ¡IMPORT NECESARIO!
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; // <-- ¡IMPORT NECESARIO!

@RestController
@RequestMapping("/api/admin") // ¡Ruta base protegida!
public class AdminController {

    private final AdminService adminService;

    // Inyección por constructor (la mejor práctica)
    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Endpoint para que un ADMIN cree un nuevo usuario (Supervisor o Técnico).
     *
     * Se accede vía: POST /api/admin/usuarios
     *
     * Protegido por la regla .requestMatchers("/api/admin/**").hasRole("ADMIN")
     * en SecurityConfig.
     */
    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody CrearUsuarioRequestDTO dto) {
        try {
            // 1. Llama al servicio para crear el usuario
            Usuario nuevoUsuario = adminService.crearNuevoUsuario(dto);

            // 2. Devuelve 201 Created (Éxito) con el usuario creado
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);

        } catch (IllegalStateException e) {
            // 3. Captura errores de negocio (ej. "usuario ya existe")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 4. Captura otros errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear el usuario.");
        }
    }

    // --- ¡AQUÍ ESTÁ EL NUEVO MÉTODO AÑADIDO! ---

    /**
     * Endpoint GET para que el Admin obtenga una lista de todos
     * los supervisores (y admins) disponibles para asignar.
     * Se accede vía: GET /api/admin/supervisores
     * Protegido por la ruta base "/api/admin/**".
     */
    @GetMapping("/supervisores")
    public ResponseEntity<List<Usuario>> getSupervisores() {
        // (Nota: @JsonIgnoreProperties en la Entidad Usuario evitará el bucle)
        return ResponseEntity.ok(adminService.obtenerSupervisores());
    }
}