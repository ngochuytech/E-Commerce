package com.example.e_commerce_techshop.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SendGridEmailService {

    @Value("${spring.mail.password}")
    private String sendGridApiKey;

    @Value("${spring.mail.properties.from}")
    private String fromAddress;

    @Value("${spring.mail.properties.from-name}")
    private String senderName;

    public void sendEmail(String toEmail, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromAddress, senderName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new IOException("SendGrid API error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Failed to send email via SendGrid: " + ex.getMessage(), ex);
        }
    }
}