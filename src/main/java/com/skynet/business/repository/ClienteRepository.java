// Archivo: ClienteRepository.java
package com.skynet.business.repository;

import com.skynet.business.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}