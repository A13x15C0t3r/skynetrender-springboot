package com.skynet.business.service;

import com.skynet.business.model.Visita;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    /**
     * Genera un PDF simple para una visita y lo devuelve como un stream de bytes.
     * Este método se llama "en memoria" (no guarda archivos en el servidor).
     */
    public byte[] generarPdfVisita(Visita visita) throws IOException { {

        // 1. Crea un documento PDF vacío en la memoria
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // 2. Define un formateador de fecha para que sea legible
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm 'horas'");

            // 3. Prepara un "lápiz" (content stream) para "escribir" en la página
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // --- Escribe el Título ---
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.setLeading(18.5f); // Espacio entre líneas
                contentStream.newLineAtOffset(50, 750); // Posición inicial (x, y)

                contentStream.showText("Reporte de Visita (ID: " + visita.getId() + ")");
                contentStream.newLine();
                contentStream.newLine();

                // --- Escribe los Detalles de la Visita ---
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);

                contentStream.showText("Cliente: " + visita.getCliente().getNombreEmpresa());
                contentStream.newLine();
                contentStream.showText("Contacto: " + visita.getCliente().getPersonaContacto());
                contentStream.newLine();
                contentStream.showText("Técnico Asignado: " + visita.getTecnico().getUsername());
                contentStream.newLine();
                contentStream.showText("Supervisor: " + visita.getSupervisor().getUsername());
                contentStream.newLine();
                contentStream.newLine();

                // --- Escribe las Fechas ---
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 12); // Cursiva
                contentStream.showText("Check-In: " + visita.getFechaHoraIngreso().format(formatter));
                contentStream.newLine();
                contentStream.showText("Check-Out: " + visita.getFechaHoraEgreso().format(formatter));
                contentStream.newLine();
                contentStream.newLine();

                // --- Escribe el Reporte Final ---
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("Reporte del Técnico:");
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                // (Nota: PDFBox simple no maneja saltos de línea automáticos,
                // para un reporte real necesitarías una lógica más avanzada)
                contentStream.showText(visita.getReporteFinal());
                contentStream.newLine();

                // 4. Termina de escribir
                contentStream.endText();
            } // El 'try-with-resources' cierra el contentStream

            // 5. Guarda el PDF en un stream de bytes en memoria (no en un archivo)
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            // 6. Devuelve los bytes crudos del PDF
            return out.toByteArray();

        } // El 'try-with-resources' cierra el document
    }
    }
}