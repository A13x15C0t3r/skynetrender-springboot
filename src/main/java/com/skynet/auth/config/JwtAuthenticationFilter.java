package com.skynet.auth.config;

import com.skynet.auth.service.CustomUserDetailsService;
import com.skynet.auth.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // Importante
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Este es el filtro que interceptará TODAS las peticiones
 * para validar el token JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener el token de la petición HTTP
        String token = getJwtFromRequest(request);

        // 2. Validar el token
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // 3. Obtener el username del token
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 4. Cargar el usuario (con sus roles) desde la BD
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // 5. Crear un objeto de autenticación
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. Establecer la autenticación en el Contexto de Seguridad de Spring
            // ¡Esto es lo que "inicia sesión" al usuario para esta petición!
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 7. Continuar con el resto de los filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Método de ayuda para extraer el "Bearer Token" de la
     * cabecera "Authorization".
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Verifica que la cabecera exista y empiece con "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Devuelve solo el token (sin el "Bearer ")
            return bearerToken.substring(7, bearerToken.length());
        }

        return null;
    }
}