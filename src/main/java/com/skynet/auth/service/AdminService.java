package com.skynet.auth.service;

import com.skynet.auth.dto.CrearUsuarioRequestDTO;
import com.skynet.auth.model.Rol;
import com.skynet.auth.model.Usuario;
import com.skynet.auth.repository.RolRepository;
import com.skynet.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List; // <-- Asegúrate de importar List
import java.util.Set;

@Service
public class AdminService {

    // --- Inyección por Constructor (la forma moderna) ---
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lógica de negocio para crear un nuevo usuario (Supervisor o Técnico).
     * @Transactional asegura que si algo falla, la operación se revierte.
     */
    @Transactional
    public Usuario crearNuevoUsuario(CrearUsuarioRequestDTO dto) {

        // 1. Validar que el username (correo) no exista
        if (usuarioRepository.findByUsername(dto.getCorreo()).isPresent()) {
            throw new IllegalStateException("Error: El correo " + dto.getCorreo() + " ya está en uso.");
        }

        // 2. Buscar el Rol en la base de datos (ej. "ROLE_SUPERVISOR")
        Rol rolUsuario = rolRepository.findByNombre(dto.getRolNombre())
                .orElseThrow(() -> new RuntimeException("Error: El rol '" + dto.getRolNombre() + "' no se encontró."));

        // 3. Crear la nueva entidad Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(dto.getNombre());
        nuevoUsuario.setCargo(dto.getCargo());
        nuevoUsuario.setTelefono(dto.getTelefono());
        nuevoUsuario.setUsername(dto.getCorreo()); // Usamos el correo como username
        nuevoUsuario.setActivo(true);

        // 4. Encriptar la contraseña (¡NUNCA guardar en texto plano!)
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 5. Asignar el rol
        nuevoUsuario.setRoles(Collections.singleton(rolUsuario));

        // 6. Si el nuevo usuario es un TÉCNICO y se le asignó un supervisor...
        if (dto.getRolNombre().equals("ROLE_TECNICO") && dto.getSupervisorUsername() != null && !dto.getSupervisorUsername().isBlank()) {

            Usuario supervisor = usuarioRepository.findByUsername(dto.getSupervisorUsername())
                    .orElseThrow(() -> new RuntimeException("Supervisor asignado no encontrado: " + dto.getSupervisorUsername()));

            boolean esSupervisor = supervisor.getRoles().stream()
                    .anyMatch(r -> r.getNombre().equals("ROLE_SUPERVISOR") || r.getNombre().equals("ROLE_ADMIN"));

            if (!esSupervisor) {
                throw new IllegalStateException("El usuario " + dto.getSupervisorUsername() + " no tiene permisos de supervisor.");
            }

            nuevoUsuario.setSupervisorAsignado(supervisor);
        }

        // 7. Guardar el nuevo usuario en la base de datos
        return usuarioRepository.save(nuevoUsuario);
    }

    // --- ¡AQUÍ ESTÁ LA NUEVA LÓGICA A AÑADIR! ---

    /**
     * Obtiene una lista de todos los usuarios que tienen el rol de supervisor o admin.
     * Esto es para que el Admin pueda asignarlos.
     */
    public List<Usuario> obtenerSupervisores() {
        // 1. Llama al nuevo método del repositorio
        List<Usuario> supervisores = usuarioRepository.findByRoles_Nombre("ROLE_SUPERVISOR");

        // 2. También trae a los Admins (ya que también pueden supervisar)
        List<Usuario> admins = usuarioRepository.findByRoles_Nombre("ROLE_ADMIN");

        // 3. Combina las dos listas
        supervisores.addAll(admins);

        return supervisores;
    }
}