package com.skynet.auth.controller;

import com.skynet.auth.dto.AuthResponseDTO;
import com.skynet.auth.dto.LoginRequestDTO;
import com.skynet.auth.service.JwtTokenProvider; // Asegúrate de importar tu clase

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // La ruta base que permitimos en SecurityConfig
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Endpoint de Login.
     * Recibe un username y password, y si son correctos,
     * devuelve un Token JWT.
     */

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {

        // 1. Creamos un objeto de autenticación con las credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Si la autenticación fue exitosa, la guardamos en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generamos el token JWT usando nuestro JwtTokenProvider
        String token = jwtTokenProvider.generateToken(authentication);

        // 4. Devolvemos el token en nuestra respuesta DTO
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }
}