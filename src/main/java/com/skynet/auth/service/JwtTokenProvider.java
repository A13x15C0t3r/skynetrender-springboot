package com.skynet.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component // Le dice a Spring que esta clase es un componente gestionado.
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    // 1. La Llave Secreta (inyectada desde application.yml)
    private final Key jwtSecretKey;

    // 2. La Expiración (inyectada desde application.yml)
    @Value("${skynet.security.jwt.expiration}")
    private long jwtExpirationInMs;

    // Un nombre personalizado para guardar los roles dentro del token
    private static final String ROLES_CLAIM = "roles";

    /**
     * Constructor: Lee la llave secreta del .yml y la convierte
     * de un String Base64 a un objeto 'Key' que la librería puede usar.
     */
    public JwtTokenProvider(@Value("${skynet.security.jwt.secret-key}") String secretKeyString) {
        // Decodifica la llave Base64
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        // La convierte en una llave criptográfica segura para el algoritmo HMAC-SHA
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un nuevo token JWT para un usuario autenticado.
     */
    public String generateToken(Authentication authentication) {
        // Obtenemos el nombre de usuario principal (ej. "admin")
        String username = authentication.getName();

        // Obtenemos los roles (ej. "ROLE_ADMIN", "ROLE_TECNICO")
        // y los unimos en un solo string separado por comas.
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        //
        // Usamos el "builder" de JJWT para crear el token
        return Jwts.builder()
                // Payload: "Subject" (Sujeto) - El usuario
                .setSubject(username)

                // Payload: "Claims" (Datos) - Agregamos nuestros roles personalizados
                .claim(ROLES_CLAIM, roles)

                // Payload: "Issued At" (Emitido) - Cuándo se creó
                .setIssuedAt(now)

                // Payload: "Expiration" (Expiración) - Cuándo vence
                .setExpiration(expiryDate)

                // Signature: (Firma) - Algoritmo y nuestra llave secreta
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)

                // Construimos el String final (ej. eyJhbGciOi...)
                .compact();
    }

    /**
     * Extrae el nombre de usuario (el "Subject") del token JWT.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey) // Usa la misma llave para verificar la firma
                .build()
                .parseClaimsJws(token) // Valida la firma y decodifica el contenido
                .getBody();

        return claims.getSubject();
    }

    /**
     * Valida si un token es auténtico (firma correcta) y no ha expirado.
     */
    public boolean validateToken(String token) {
        try {
            // Intenta "parsear" (leer) el token.
            // Si la firma es inválida, la llave es incorrecta, o está expirado,
            // la librería lanzará una excepción.
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token);

            // Si llega aquí, el token es válido.
            return true;
        } catch (SignatureException ex) {
            log.error("JWT: Firma inválida.");
        } catch (MalformedJwtException ex) {
            log.error("JWT: Token malformado.");
        } catch (ExpiredJwtException ex) {
            log.error("JWT: Token ha expirado.");
        } catch (UnsupportedJwtException ex) {
            log.error("JWT: Token no soportado.");
        } catch (IllegalArgumentException ex) {
            log.error("JWT: Claims (datos) están vacíos.");
        }

        // Si ocurrió cualquier excepción, el token no es válido.
        return false;
    }
}