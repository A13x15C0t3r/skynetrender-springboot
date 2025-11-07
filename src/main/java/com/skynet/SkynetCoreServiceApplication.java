package com.skynet; // ¡El paquete raíz!

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkynetCoreServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(SkynetCoreServiceApplication.class, args);
    }
}