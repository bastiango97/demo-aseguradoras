package com.example.workflow;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;

@Component
public class CorreoDeAvisoDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Retrieve data from process variables
        String nombreAgente = (String) execution.getVariable("nomAgente");
        String correoAgente = (String) execution.getVariable("correoAgente");
        String nombreCliente = (String) execution.getVariable("nomCliente");
        String marca = (String) execution.getVariable("marca");
        String modelo = (String) execution.getVariable("modelo");
        String year = (String) execution.getVariable("year");

        // Email configuration
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "demo.agencias.camunda7@gmail.com";
        String password = "osji wsyo pibg rhnm";  // App password provided by you

        String subject = "Cliente No Interesado - " + nombreCliente;

        // Construct the email body
        String body = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                + "<h2 style=\"color: #D9534F; text-align: center;\">Aviso de Cliente No Interesado</h2>"
                + "<p style=\"font-size: 16px; color: #333;\">Estimado " + nombreAgente + ",</p>"
                + "<p style=\"font-size: 16px; color: #333;\">Le informamos que el cliente " + nombreCliente + " ha decidido no continuar con la cotización de seguro para su auto "
                + marca + " " + modelo + " (" + year + ").</p>"
                + "<p style=\"font-size: 16px; color: #333;\">El cliente no ha proporcionado un motivo específico para rechazar la cotización.</p>"
                + "<p style=\"font-size: 16px; color: #333;\">Lamentamos que esta oportunidad no se haya concretado. Por favor, no dude en contactar al cliente si considera que puede haber un malentendido o si quiere ofrecerle otra alternativa.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; margin-top: 30px;\">Gracias por su esfuerzo y dedicación.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; font-weight: bold;\">Su Compañía de Seguros</p>"
                + "</div>";

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
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoAgente));
        message.setSubject(subject);

        // Create a MimeBodyPart for the HTML content
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");

        // Create a multipart message and add the body part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        // Set the complete message parts
        message.setContent(multipart);

        // Send the email
        Transport.send(message);
    }
}
