package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:AutoCare Platform}")
    private String fromName;

    // Public API methods
    public boolean sendApprovalEmail(User user) {
        logger.info("📧 Attempting to send approval email to: {}", user.getUsername());
        return sendEmail(user, APPROVED);
    }

    public boolean sendRejectionEmail(User user) {
        logger.info("📧 Attempting to send rejection email to: {}", user.getUsername());
        return sendEmail(user, REJECTED);
    }

    // Core email sending logic
    private boolean sendEmail(User user, String emailType) {
        try {
            validateConfiguration();

            String subject = buildSubject(emailType);
            String htmlContent = buildEmailContent(user, emailType);

            return sendEmailInternal(user.getUsername(), subject, htmlContent);

        } catch (Exception e) {
            logger.error("❌ Failed to send {} email to {}: {}", emailType, user.getUsername(), e.getMessage());
            return false;
        }
    }

    private boolean sendEmailInternal(String toEmail, String subject, String htmlContent) {
        try {
            logger.info("🔧 Starting email send process to: {}", toEmail);

            if (!isValidEmail(toEmail)) {
                logger.error("❌ Invalid recipient email: {}", toEmail);
                return false;
            }

            Mail mail = buildMail(toEmail, subject, htmlContent);
            Response response = sendViaSendGrid(mail);

            return handleResponse(response);

        } catch (IOException e) {
            logger.error("❌ IOException sending email: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("❌ Unexpected error sending email: {}", e.getMessage());
            return false;
        }
    }

    // Email construction helpers
    private Mail buildMail(String toEmail, String subject, String htmlContent) {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        return new Mail(from, subject, to, content);
    }

    private String buildSubject(String emailType) {
        return emailType.equals(APPROVED)
                ? "Account Approved - Welcome to AutoCare Platform"
                : "Account Registration Update - AutoCare Platform";
    }

    private String buildEmailContent(User user, String emailType) {
        return emailType.equals(APPROVED)
                ? buildApprovalEmailContent(user)
                : buildRejectionEmailContent(user);
    }

    // SendGrid integration
    private Response sendViaSendGrid(Mail mail) throws IOException {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        logger.info("🚀 Sending email via SendGrid...");
        return sg.api(request);
    }

    private boolean handleResponse(Response response) {
        int statusCode = response.getStatusCode();
        logger.info("📨 SendGrid Response - Status: {}, Body: {}", statusCode, response.getBody());

        if (statusCode >= 200 && statusCode < 300) {
            logger.info("✅ Email sent successfully!");
            return true;
        } else {
            logger.error("❌ SendGrid API error - Status: {}, Response: {}", statusCode, response.getBody());
            return false;
        }
    }

    // Validation methods
    private void validateConfiguration() throws IOException {
        logger.info("🔍 Validating email configuration...");

        if (sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
            throw new IOException("SendGrid API key is not configured");
        }

        if (sendGridApiKey.equals("your_sendgrid_api_key_here")) {
            throw new IOException("SendGrid API key is still using placeholder value");
        }

        if (!sendGridApiKey.startsWith("SG.")) {
            throw new IOException("Invalid SendGrid API key format");
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            throw new IOException("From email is not configured");
        }

        logger.info("✅ Email configuration validated successfully");
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && email.contains("@");
    }

    // Email template builders
    private String buildApprovalEmailContent(User user) {
        String firstName = user.getFname() != null ? user.getFname() : "User";
        String company = user.getcName() != null ? user.getcName() : "N/A";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Account Approved!</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>Your account has been <strong>approved</strong> and is now active on AutoCare Platform.</p>
                        <p><strong>Company:</strong> %s</p>
                        <p>You can now login and start using all features of our platform.</p>
                        <p><a href="http://localhost:3000/signin" style="color: #4CAF50;">Click here to login</a></p>
                    </div>
                    <div class="footer">
                        <p>AutoCare Platform Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, company);
    }

    private String buildRejectionEmailContent(User user) {
        String firstName = user.getFname() != null ? user.getFname() : "User";
        String reason = user.getRejectionReason() != null ?
                user.getRejectionReason() : "Your registration did not meet our requirements.";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .reason { background: #ffebee; padding: 15px; margin: 15px 0; border-left: 4px solid #f44336; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Registration Status Update</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>After reviewing your registration, we are unable to approve your account at this time.</p>
                        <div class="reason">
                            <strong>Reason:</strong><br>
                            %s
                        </div>
                        <p>If you have questions, please contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>AutoCare Platform Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, reason);
    }
}