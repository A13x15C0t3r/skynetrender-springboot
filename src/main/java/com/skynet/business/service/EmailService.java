package com.skynet.business.service;

import com.skynet.business.model.Visita;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// ¡Añadimos los imports que faltaban para la lista!
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Envía un email de reporte de visita completada CON el PDF adjunto.
     * ¡VERSIÓN ACTUALIZADA!
     * Ahora envía el correo tanto al Supervisor como al Cliente.
     */
    public void enviarReporteVisitaConAdjunto(Visita visita, byte[] pdfBytes) {

        // --- 1. Construir la lista de destinatarios ---
        List<String> destinatarios = new ArrayList<>();

        // Destinatario 1: El Supervisor (siempre)
        String supervisorEmail = visita.getSupervisor().getUsername();
        destinatarios.add(supervisorEmail);

        // Destinatario 2: El Cliente (opcional)
        String clienteEmail = visita.getCliente().getCorreo();

        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN DE JAVA 8! ---
        // Reemplazamos !clienteEmail.isBlank() (Java 11)
        // por su equivalente en Java 8.
        if (clienteEmail != null && !clienteEmail.trim().isEmpty()) {
            destinatarios.add(clienteEmail);
        }
        // --- FIN DE LA CORRECCIÓN ---

        // --- 2. Preparar el contenido del email ---
        String subject = "Visita Completada (ID: " + visita.getId() + ") - Cliente: " + visita.getCliente().getNombreEmpresa();
        String texto = "Se ha completado la visita técnica.\n\n"
                + "Cliente: " + visita.getCliente().getNombreEmpresa() + "\n"
                + "Técnico: " + visita.getTecnico().getUsername() + "\n"
                + "Fecha de Egreso: " + visita.getFechaHoraEgreso().toString() + "\n\n"
                + "Reporte:\n" + visita.getReporteFinal() + "\n\n"
                + "Se adjunta el reporte en formato PDF.";

        try {
            // 1. Crea un MimeMessage (para adjuntos)
            MimeMessage message = mailSender.createMimeMessage();

            // 2. Usa MimeMessageHelper (true = multipart)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);

            // 3. ¡CAMBIO CLAVE!
            // Convertimos la Lista de destinatarios en un Array de String
            helper.setTo(destinatarios.toArray(new String[0]));

            helper.setSubject(subject);
            helper.setText(texto);

            // 4. Añade el adjunto
            helper.addAttachment(
                    "reporte-visita-" + visita.getId() + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            // 5. Envía el email
            mailSender.send(message);

            System.out.println(">>> Email de reporte CON ADJUNTO enviado exitosamente a: " + String.join(", ", destinatarios));

        } catch (MessagingException e) {
            System.err.println(">>> Error al enviar email con adjunto: " + e.getMessage());
        }
    }
}