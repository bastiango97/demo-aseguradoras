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
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/accept/" + userTaskId + "\" style=\"background-color: #28a745; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Aceptar Póliza</a>"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/reject/" + userTaskId + "\" style=\"background-color: #dc3545; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Rechazar Póliza</a>"
                + "<a href=\"https://camunda-task-completion.onrender.com/complete-task/request-changes/" + userTaskId + "\" style=\"background-color: #ffc107; color: white; padding: 15px 30px; border-radius: 5px; text-decoration: none; margin: 10px; display: inline-block;\">Solicitar Cambios</a>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; margin-top: 30px;\">Gracias por confiar en nosotros para su seguro de auto.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center;\">Si tiene alguna pregunta, no dude en contactarnos.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; font-weight: bold;\">Su Compañía de Seguros</p>"
                + "</div>";


        sendEmail(correoCliente, subjectClient, bodyClient, host, port, username, password);

        // Prepare email for the agent
        String subjectAgent = "Nueva Cotización - " + prioridad + " - Prima: $" + prima;
        String bodyAgent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                + "<h2 style=\"color: #2E86C1; text-align: center;\">Nueva Cotización de Seguro de Auto</h2>"
                + "<p style=\"font-size: 16px; color: #333;\">Estimado Agente,</p>"
                + "<p style=\"font-size: 16px; color: #333;\">A continuación se presentan los detalles de la cotización:</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Cliente:</strong> " + nombreCliente + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Correo del Cliente:</strong> " + correoCliente + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Teléfono:</strong> " + (String) execution.getVariable("telefonoCliente") + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Marca:</strong> " + marca + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Modelo:</strong> " + modelo + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Año:</strong> " + year + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Prima:</strong> $" + prima + "</p>"
                + "<p style=\"font-size: 16px; color: #333;\"><strong>Prioridad:</strong> " + prioridad + "</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; margin-top: 30px;\">Por favor contacte al cliente para dar seguimiento.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center; font-weight: bold;\">Su Compañía de Seguros</p>"
                + "</div>";

        sendEmail(correoAgente, subjectAgent, bodyAgent, host, port, username, password);
    }

    private void sendEmail(String recipient, String subject, String body, String host, String port, String username, String password) throws MessagingException {
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

        // Create a multipart message and add the body part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        // Set the complete message parts
        message.setContent(multipart);

        // Send the email
        Transport.send(message);
    }
}
