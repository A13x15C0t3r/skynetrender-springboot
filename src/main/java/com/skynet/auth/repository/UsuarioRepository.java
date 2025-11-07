package com.skynet.auth.repository;

import com.skynet.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- ¡IMPORTA ESTO!
import org.springframework.data.repository.query.Param; // <-- ¡IMPORTA ESTO!

import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN CLAVE! ---
    /**
     * Le decimos a JPA que cree una consulta personalizada.
     * "JOIN FETCH u.roles" le ORDENA a Hibernate que traiga
     * los roles en la misma consulta, solucionando el problema del 403.
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);


    /**
     * Busca TODOS los usuarios que tengan un rol específico.
     */
    List<Usuario> findByRoles_Nombre(String rolNombre);

    /**
     * Busca usuarios que tengan un rol específico Y que estén
     * asignados a un supervisor específico.
     */
    List<Usuario> findByRoles_NombreAndSupervisorAsignado(String rolNombre, Usuario supervisor);
}