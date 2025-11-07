package com.skynet.auth.service;

import com.skynet.auth.model.Rol;
import com.skynet.auth.model.Usuario;
import com.skynet.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ¡EL IMPORT CLAVE!

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional // <-- ¡LA ANOTACIÓN CLAVE!
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con username: " + username));

        // Gracias a @Transactional, esta llamada SÍ cargará los roles
        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(usuario.getRoles());

        return new User(usuario.getUsername(), usuario.getPassword(), authorities);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Rol> roles) {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());
    }
}