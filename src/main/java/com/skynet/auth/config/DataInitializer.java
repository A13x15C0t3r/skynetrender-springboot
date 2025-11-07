package com.skynet.auth.config;

import com.skynet.auth.model.Rol;
import com.skynet.auth.model.Usuario;
import com.skynet.auth.repository.RolRepository;
import com.skynet.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
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

        // --- 1. Crear Roles (Esto SÍ lo necesitamos) ---
        // (El admin necesita que 'ROLE_ADMIN' exista)
        if (!rolRepository.findByNombre("ROLE_ADMIN").isPresent()) {
            rolRepository.save(new Rol("ROLE_ADMIN"));
        }
        if (!rolRepository.findByNombre("ROLE_SUPERVISOR").isPresent()) {
            rolRepository.save(new Rol("ROLE_SUPERVISOR"));
        }
        if (!rolRepository.findByNombre("ROLE_TECNICO").isPresent()) {
            rolRepository.save(new Rol("ROLE_TECNICO"));
        }

        // --- 2. Crear Usuario Admin (Este SÍ lo queremos) ---
        if (!usuarioRepository.findByUsername("admin").isPresent()) {
            Rol adminRol = rolRepository.findByNombre("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN no se pudo crear o encontrar."));

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador del Sistema");
            admin.setCargo("Administrador");
            admin.setRoles(Collections.singleton(adminRol));
            usuarioRepository.save(admin);

            System.out.println(">>> ¡Usuario 'admin' creado exitosamente! <<<");
        }

        // --- 3. Crear Usuario Supervisor 1 (COMENTADO) ---
        /*
        if (!usuarioRepository.findByUsername("supervisor1").isPresent()) {
            Rol supervisorRol = rolRepository.findByNombre("ROLE_SUPERVISOR")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_SUPERVISOR no encontrado"));

            Usuario supervisor = new Usuario();
            supervisor.setUsername("supervisor1");
            supervisor.setPassword(passwordEncoder.encode("sup123"));
            supervisor.setNombre("Supervisor Uno");
            supervisor.setCargo("Supervisor");
            supervisor.setRoles(Collections.singleton(supervisorRol));
            usuarioRepository.save(supervisor);
            System.out.println(">>> ¡Usuario 'supervisor1' creado exitosamente! <<<");
        }
        */

        // --- 4. Crear Usuario Técnico 1 (COMENTADO) ---
        /*
        if (!usuarioRepository.findByUsername("tecnico1").isPresent()) {
            Rol tecnicoRol = rolRepository.findByNombre("ROLE_TECNICO")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_TECNICO no encontrado"));

            Usuario tecnico = new Usuario();
            tecnico.setUsername("tecnico1");
            tecnico.setPassword(passwordEncoder.encode("tec123"));
            tecnico.setNombre("Tecnico Uno");
            tecnico.setCargo("Técnico");
            tecnico.setRoles(Collections.singleton(tecnicoRol));
            usuarioRepository.save(tecnico);
            System.out.println(">>> ¡Usuario 'tecnico1' creado exitosamente! <<<");
        }
        */

        // --- 5. Crear Supervisor 2 (con email) (COMENTADO) ---
        /*
        if (!usuarioRepository.findByUsername("alexmarthketnest@gmail.com").isPresent()) {
            Rol supervisorRol = rolRepository.findByNombre("ROLE_SUPERVISOR")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_SUPERVISOR no encontrado"));

            Usuario supervisor2 = new Usuario();
            supervisor2.setUsername("alexmarthketnest@gmail.com");
            supervisor2.setPassword(passwordEncoder.encode("super456"));
            supervisor2.setNombre("Alex Supervisor");
            supervisor2.setCargo("Supervisor de Cuentas");
            supervisor2.setRoles(Collections.singleton(supervisorRol));
            usuarioRepository.save(supervisor2);

            System.out.println(">>> ¡Usuario 'alexmarthketnest@gmail.com' creado exitosamente! <<<");
        }
        */
    }
}