package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;

public class EmailService {

    private final MailClient mailClient;

    public EmailService(Vertx vertx) {
        MailConfig config = new MailConfig()
                .setHostname("smtp.gmail.com")
                .setPort(587)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername("YOUR_MAIL_HERE")         // ✅ Your Gmail address
                .setPassword("YOUR_GAPP_PASS");   // ✅ Paste your app password here

        mailClient = MailClient.createShared(vertx, config, "email-pool");
    }

    public void sendPasswordEmail(String to, String password) {
        MailMessage message = new MailMessage()
                .setFrom("SAME_MAIL_AGAIN")
                .setTo(to)
                .setSubject("Your Course Registration Password")
                .setText("Welcome! Your password for course registration is: " + password);

        mailClient.sendMail(message, result -> {
            if (result.succeeded()) {
                System.out.println("✅ Email sent to " + to);
            } else {
                System.err.println("❌ Email failed: " + result.cause().getMessage());
            }
        });
    }
}
