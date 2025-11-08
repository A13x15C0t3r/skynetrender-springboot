package com.skynet.auth.config;

// --- Imports de Java y Jakarta ---
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

// --- Imports de Spring ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- ¡EL IMPORT CLAVE!
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Esta es la configuración de seguridad principal (FilterChain).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // --- 1. CONFIGURACIÓN DE CORS ---
                // Le dice a Spring que confíe en la URL de desarrollo de Vite
                // ... dentro del método filterChain
                .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();

                        // Permitimos el origen local (por si acaso) y el origen público (*)
                        config.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "*" // <-- Permite CUALQUIER origen (Necesario si no sabes la URL final)
                        ));

                        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        config.setAllowedHeaders(List.of("*"));
                        config.setAllowCredentials(true);
                        return config;
                    }
                }))
// ...

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // --- 2. REGLAS DE AUTORIZACIÓN ---
                .authorizeHttpRequests(authz -> authz

                        // ¡LA LÍNEA MÁGICA QUE ARREGLA EL "CARGANDO..."!
                        // Permite todas las peticiones "preflight" OPTIONS del navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Tus reglas normales ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/clientes/**").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/supervisor/**").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/api/tecnico/**").hasRole("TECNICO")

                        .anyRequest().authenticated()
                )

                .httpBasic(basic -> basic.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}