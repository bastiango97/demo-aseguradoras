package com.example.workflow;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class sendQuote implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Retrieve variables from the process instance
        String nombreCliente = (String) execution.getVariable("nomCliente");
        String correoCliente = (String) execution.getVariable("correoCliente");
        String marca = (String) execution.getVariable("marca");
        String modelo = (String) execution.getVariable("modelo");
        String year = (String) execution.getVariable("year");

        Object primaObj = execution.getVariable("prima");
        String prima;

        if (primaObj instanceof Integer) {
            prima = String.valueOf(primaObj);
        } else if (primaObj instanceof String) {
            prima = (String) primaObj;
        } else {
            throw new IllegalArgumentException("Variable 'prima' is neither an Integer nor a String");
        }

        // Get process instance ID for constructing the task completion URL
        String processInstanceId = execution.getProcessInstanceId();

        // Generate PDF file in memory
        ByteArrayOutputStream pdfOutputStream = generatePdf(nombreCliente, marca, modelo, year, prima);

        // Email configuration
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "demo.agencias.camunda7@gmail.com";
        String password = "osji wsyo pibg rhnm";  // App password provided by you

        // Prepare email content for the client
        String subjectClient = "Hemos calculado tu cotización de seguro";
        String bodyClient = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                + "<h2 style=\"color: #2E86C1; text-align: center;\">Cotización de Seguro de Auto</h2>"
                + "<p style=\"font-size: 16px; color: #333;\">Estimado " + nombreCliente + ",</p>"
                + "<p style=\"font-size: 16px; color: #333;\">Hemos calculado tu cotización para el auto " + marca + " " + modelo + " (" + year + ").</p>"
                + "<p style=\"font-size: 16px; color: #333;\">El valor estimado de la prima es de $" + prima + ".</p>"
                + "<p style=\"font-size: 16px; color: #333;\">Puedes revisar la cotización completa y tomar una decisión usando los siguientes enlaces:</p>"
                + "<div style=\"text-align: center; margin-top: 30px;\">"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/accept/" + processInstanceId + "\" style=\"background-color: #28a745; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Aceptar Cotización</a>"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/reject/" + processInstanceId + "\" style=\"background-color: #dc3545; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Rechazar Cotización</a>"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/request-changes/" + processInstanceId + "\" style=\"background-color: #ffc107; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Solicitar Cambios</a>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; margin-top: 30px;\">Gracias por confiar en nosotros para tu seguro de auto.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; font-weight: bold;\">Su Compañía de Seguros</p>"
                + "</div>";

        sendEmail(correoCliente, subjectClient, bodyClient, pdfOutputStream, "Cotizacion_Seguro.pdf", host, port, username, password);
    }

    private ByteArrayOutputStream generatePdf(String nombreCliente, String marca, String modelo, String year, String prima) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Set up colors
        Color headerColor = new DeviceRgb(63, 169, 245);
        Color bgColor = new DeviceRgb(240, 240, 240);

        // Set up fonts
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

        // Add header
        Paragraph header = new Paragraph("Cotización de Seguro de Auto")
                .setFont(boldFont)
                .setFontSize(20)
                .setFontColor(headerColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(header);

        // Customer and car details
        float[] columnWidths = {1, 2};
        Table detailsTable = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth();

        detailsTable.addCell(new Cell().add(new Paragraph("Nombre del Cliente:"))
                .setFont(boldFont).setBackgroundColor(bgColor));
        detailsTable.addCell(new Cell().add(new Paragraph(nombreCliente))
                .setFont(regularFont).setBackgroundColor(bgColor));

        detailsTable.addCell(new Cell().add(new Paragraph("Marca y Modelo del Auto:"))
                .setFont(boldFont).setBackgroundColor(bgColor));
        detailsTable.addCell(new Cell().add(new Paragraph(marca + " " + modelo + " (" + year + ")"))
                .setFont(regularFont).setBackgroundColor(bgColor));

        detailsTable.addCell(new Cell().add(new Paragraph("Paquete de Cobertura:"))
                .setFont(boldFont).setBackgroundColor(bgColor));
        detailsTable.addCell(new Cell().add(new Paragraph("Cobertura Completa"))
                .setFont(regularFont).setBackgroundColor(bgColor));

        document.add(detailsTable.setMarginBottom(20));

        // Pricing Details
        Paragraph pricingHeader = new Paragraph("Detalles de la Cotización")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(headerColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(pricingHeader);

        float[] priceColumnWidths = {1, 2, 1};
        Table priceTable = new Table(UnitValue.createPercentArray(priceColumnWidths))
                .useAllAvailableWidth();

        priceTable.addCell(new Cell().add(new Paragraph("Descripción"))
                .setFont(boldFont).setBackgroundColor(bgColor));
        priceTable.addCell(new Cell().add(new Paragraph("Detalles"))
                .setFont(boldFont).setBackgroundColor(bgColor));
        priceTable.addCell(new Cell().add(new Paragraph("Monto (MXN)"))
                .setFont(boldFont).setBackgroundColor(bgColor).setTextAlignment(TextAlignment.RIGHT));

        priceTable.addCell(new Cell().add(new Paragraph("Prima Base"))
                .setFont(regularFont));
        priceTable.addCell(new Cell().add(new Paragraph("Prima anual básica para el vehículo"))
                .setFont(regularFont));
        priceTable.addCell(new Cell().add(new Paragraph("$" + prima))
                .setFont(regularFont).setTextAlignment(TextAlignment.RIGHT));

        priceTable.addCell(new Cell().add(new Paragraph("Cobertura Adicional"))
                .setFont(regularFont));
        priceTable.addCell(new Cell().add(new Paragraph("Asistencia Vial, Cobertura de Auto de Reemplazo"))
                .setFont(regularFont));
        priceTable.addCell(new Cell().add(new Paragraph("$2,000"))
                .setFont(regularFont).setTextAlignment(TextAlignment.RIGHT));

        priceTable.addCell(new Cell().add(new Paragraph("Descuento"))
                .setFont(regularFont));
        priceTable.addCell(new Cell().add(new Paragraph("Descuento por no siniestralidad"))
                .setFont(regularFont));
        priceTable.addCell(new Cell().add(new Paragraph("-$1,000"))
                .setFont(regularFont).setTextAlignment(TextAlignment.RIGHT));

        priceTable.addCell(new Cell(1, 2).add(new Paragraph("Total"))
                .setFont(boldFont).setTextAlignment(TextAlignment.RIGHT).setBackgroundColor(bgColor));
        priceTable.addCell(new Cell().add(new Paragraph("$" + prima))
                .setFont(boldFont).setTextAlignment(TextAlignment.RIGHT).setBackgroundColor(bgColor));

        document.add(priceTable.setMarginBottom(20));

        // Footer
        Paragraph footer = new Paragraph("¡Gracias por elegir nuestros servicios de seguros!")
                .setFont(regularFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
        document.add(footer);

        // Close the document
        document.close();

        return outputStream;
    }

    private void sendEmail(String recipient, String subject, String body, ByteArrayOutputStream pdfOutputStream, String pdfFileName, String host, String port, String username, String password) throws MessagingException {
        // Set up the SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Create a new session with an authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Create the email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);

        // Create a MimeBodyPart for the HTML content
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");

        // Create a MimeBodyPart for the PDF attachment
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setFileName(pdfFileName);
        attachmentBodyPart.setContent(pdfOutputStream.toByteArray(), "application/pdf");

        // Create a multipart message and add the body part and attachment
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        multipart.addBodyPart(attachmentBodyPart);

        // Set the complete message parts
        message.setContent(multipart);

        // Send the email
        Transport.send(message);
    }

}

