package com.example.workflow;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;


import com.itextpdf.layout.element.Cell;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;
import java.io.IOException;

@Component
public class EmailSenderDelegate implements JavaDelegate {

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Get process instance ID
        String processInstanceId = execution.getProcessInstanceId();

        // Get the list of tasks associated with the process instance
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();

        // Assuming there's only one user task, get its ID
        String userTaskId = tasks.get(0).getId();

        // Log the process instance ID and user task ID
        System.out.println("Process Instance ID: " + processInstanceId);
        System.out.println("User Task ID: " + userTaskId);

        // Retrieve data from form variables
        String nombreCliente = (String) execution.getVariable("nomCliente");
        String correoCliente = (String) execution.getVariable("correoCliente");
        String marca = (String) execution.getVariable("marca");
        String modelo = (String) execution.getVariable("modelo");
        String year = (String) execution.getVariable("year");
        String correoAgente = (String) execution.getVariable("correoAgente");
        String telefonoCliente = (String) execution.getVariable("telefonoCliente");

        Object primaObj = execution.getVariable("prima");
        String prima;

        if (primaObj instanceof Integer) {
            prima = String.valueOf(primaObj);
        } else if (primaObj instanceof String) {
            prima = (String) primaObj;
        } else {
            throw new IllegalArgumentException("Variable 'prima' is neither an Integer nor a String");
        }

        Object priorityObj = execution.getVariable("closureProbability");
        String prioridad;
        if (priorityObj instanceof String) {
            prioridad = (String) priorityObj;
        } else {
            throw new IllegalArgumentException("Variable 'closureProbability' is not a String");
        }

        // Generate PDF file in memory
        ByteArrayOutputStream pdfOutputStream = generatePdf(nombreCliente, marca, modelo, year, prima, prioridad);

        // Email configuration
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "demo.agencias.camunda7@gmail.com";
        String password = "osji wsyo pibg rhnm";  // App password provided by you

        // Prepare email for the client
        String subjectClient = "Acción requerida: Cotización de Seguro";
        String bodyClient = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                + "<h2 style=\"color: #2E86C1; text-align: center;\">Cotización de Seguro de Auto</h2>"
                + "<p style=\"font-size: 16px; color: #333;\">Estimado " + nombreCliente + ",</p>"
                + "<p style=\"font-size: 16px; color: #333;\">Hemos preparado una cotización para tu auto " + marca + " " + modelo + " (" + year + ") con un valor de prima de $" + prima + ".</p>"
                + "<p style=\"font-size: 16px; color: #333;\">Por favor elija una de las siguientes opciones para continuar con su cotización de seguro:</p>"
                + "<div style=\"text-align: center; margin-top: 30px;\">"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/accept/" + userTaskId + "\" style=\"background-color: #28a745; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Aceptar Cotización</a>"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/reject/" + userTaskId + "\" style=\"background-color: #dc3545; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Rechazar Cotización</a>"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/request-changes/" + userTaskId + "\" style=\"background-color: #ffc107; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Solicitar Cambios</a>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; margin-top: 30px;\">Gracias por confiar en nosotros para su seguro de auto.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center;\">Si tiene alguna pregunta, no dude en contactarnos.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; font-weight: bold;\">Su Compañía de Seguros</p>"
                + "</div>";

        sendEmail(correoCliente, subjectClient, bodyClient, pdfOutputStream, "Cotizacion_Seguro.pdf", host, port, username, password);

        // Prepare email for the agent
        String subjectAgent = "Nueva Cotización - " + prioridad + " - Prima: $" + prima;
        String bodyAgent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                + "<h2 style=\"color: #2E86C1; text-align: center;\">Nueva Cotización de Seguro de Auto</h2>"
                + "<p style=\"font-size: 16px; color: #333;\">Estimado Agente,</p>"
                + "<p style=\"font-size: 16px; color: #333;\">A continuación se presentan los detalles de la cotización:</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Cliente:</strong> " + nombreCliente + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Correo del Cliente:</strong> " + correoCliente + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Teléfono:</strong> " + telefonoCliente + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Marca:</strong> " + marca + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Modelo:</strong> " + modelo + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Año:</strong> " + year + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Prima:</strong> $" + prima + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Prioridad:</strong> " + prioridad + "</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; margin-top: 30px;\">Por favor contacte al cliente para dar seguimiento.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; font-weight: bold;\">Su Compañía de Seguros</p>"
                + "</div>";

        sendEmail(correoAgente, subjectAgent, bodyAgent, pdfOutputStream, "Cotizacion_Seguro.pdf", host, port, username, password);
    }

    private ByteArrayOutputStream generatePdf(String nombreCliente, String marca, String modelo, String year, String prima, String prioridad) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Set up colors
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
        try {
            attachmentBodyPart.attachFile(pdfFileName);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            // Handle the error accordingly, maybe log it or inform the user
        }
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


