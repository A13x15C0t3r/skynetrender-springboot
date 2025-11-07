package com.skynet.auth.config;

// --- Imports de Java y Jakarta ---
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

// --- Imports de Spring ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- ¡EL IMPORT IMPORTANTE!
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // ... (dentro de tu método filterChain en SecurityConfig.java)

                .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();

                        // ¡CAMBIO CLAVE!
                        // Permite peticiones desde CUALQUIER origen.
                        // O, para más seguridad, reemplaza "*" por tu futura URL de Vercel.
                        config.setAllowedOrigins(List.of(
                                "http://localhost:5173", // Para desarrollo local
                                "*"                      // Para el mundo (Render/Vercel)
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

                .authorizeHttpRequests(authz -> authz

                        // --- ¡ESTA ES LA LÍNEA MÁGICA QUE FALTABA! ---
                        // Le dice a Spring Security: "Permite TODAS las peticiones OPTIONS
                        // de 'sondeo' de CORS antes de revisar los tokens".
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Tus reglas normales ---
                        .requestMatchers("/api/auth/**").permitAll() // Para Login
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