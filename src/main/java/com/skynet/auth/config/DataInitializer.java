package com.skynet.auth.config;

import com.skynet.auth.model.Rol;
import com.skynet.auth.model.Usuario;
import com.skynet.auth.repository.RolRepository;
import com.skynet.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collections; // ¡EL IMPORT CLAVE!
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // --- 1. Crear Roles ---
        if (!rolRepository.findByNombre("ROLE_ADMIN").isPresent()) {
            rolRepository.save(new Rol("ROLE_ADMIN"));
        }
        if (!rolRepository.findByNombre("ROLE_SUPERVISOR").isPresent()) {
            rolRepository.save(new Rol("ROLE_SUPERVISOR"));
        }
        if (!rolRepository.findByNombre("ROLE_TECNICO").isPresent()) {
            rolRepository.save(new Rol("ROLE_TECNICO"));
        }

        // --- 2. Crear Usuario Admin ---
        if (!usuarioRepository.findByUsername("admin").isPresent()) {
            Rol adminRol = rolRepository.findByNombre("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN no se pudo crear o encontrar."));

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // ¡Encripta!
            admin.setNombre("Administrador del Sistema"); // ¡Arregla el NOT NULL!
            admin.setCargo("Administrador"); // ¡Arregla el NOT NULL!
            admin.setRoles(Collections.singleton(adminRol)); // ¡Arregla el Set.of()!
            usuarioRepository.save(admin);

            System.out.println(">>> ¡Usuario 'admin' creado exitosamente! <<<");
        }
    }
}