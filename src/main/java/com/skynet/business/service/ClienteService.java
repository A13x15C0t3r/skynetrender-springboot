// Archivo: ClienteService.java
package com.skynet.business.service;

import com.skynet.business.model.Cliente;
import com.skynet.business.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente registrarNuevoCliente(Cliente cliente) {
        // Aquí podrías agregar validaciones
        return clienteRepository.save(cliente);
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }
}