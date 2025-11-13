package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.User;
import com.sendgrid.*;
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

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:AutoCare Platform}")
    private String fromName;

    /**
     * Send approval email to user
     */
    public void sendApprovalEmail(User user) throws IOException {
        logger.info("📧 Preparing to send approval email to: {}", user.getUsername());

        String subject = "Account Approved - Welcome to AutoCare Platform";
        String htmlContent = buildApprovalEmailContent(user);

        sendEmail(user.getUsername(), subject, htmlContent);
        logger.info("✅ Approval email sent successfully to: {}", user.getUsername());
    }

    /**
     * Send rejection email to user
     */
    public void sendRejectionEmail(User user) throws IOException {
        logger.info("📧 Preparing to send rejection email to: {}", user.getUsername());

        String subject = "Account Registration Update - AutoCare Platform";
        String htmlContent = buildRejectionEmailContent(user);

        sendEmail(user.getUsername(), subject, htmlContent);
        logger.info("✅ Rejection email sent successfully to: {}", user.getUsername());
    }

    /**
     * Core method to send email via SendGrid
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) throws IOException {
        logger.info("🔧 Starting email send process...");
        logger.info("📧 To: {}", toEmail);
        logger.info("📝 Subject: {}", subject);

        // Check if SendGrid is properly configured
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            logger.error("❌ SendGrid API key is null or empty!");
            throw new IllegalStateException("SendGrid API key is not configured");
        }

        if (sendGridApiKey.equals("your_sendgrid_api_key_here")) {
            logger.error("❌ SendGrid API key is still the placeholder value!");
            throw new IllegalStateException("SendGrid API key not properly configured - still using placeholder");
        }

        logger.info("🔑 API Key configured: {}", sendGridApiKey != null && !sendGridApiKey.isEmpty());

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            logger.info("🚀 Sending request to SendGrid API...");

            Response response = sg.api(request);

            logger.info("📨 SendGrid Response - Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Email sent successfully. Status: {}", response.getStatusCode());
            } else {
                logger.error("❌ SendGrid returned error. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new IOException("SendGrid API error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            logger.error("❌ Failed to send email via SendGrid: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("❌ Unexpected error sending email: {}", e.getMessage(), e);
            throw new IOException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Build HTML content for approval email
     */
    private String buildApprovalEmailContent(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                    .button { display: inline-block; padding: 12px 30px; background: #10b981; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .success-icon { font-size: 48px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Account Approved!</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s %s,</p>
                        
                        <p>Great news! Your registration on AutoCare Platform has been <strong>approved</strong>.</p>
                        
                        <p>You can now access all features of our platform including:</p>
                        <ul>
                            <li>Post and manage vehicle advertisements</li>
                            <li>Connect with insurance and leasing companies</li>
                            <li>Access our comprehensive vehicle database</li>
                            <li>Submit and read vehicle reviews</li>
                        </ul>
                        
                        <div style="text-align: center;">
                            <a href="http://localhost:3000/signin" class="button">Login to Your Account</a>
                        </div>
                        
                        <p><strong>Your Account Details:</strong></p>
                        <ul>
                            <li>Email: %s</li>
                            <li>Company: %s</li>
                            <li>Status: Approved ✅</li>
                        </ul>
                        
                        <p>If you have any questions, feel free to contact our support team.</p>
                        
                        <p>Best regards,<br>
                        <strong>AutoCare Platform Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>&copy; 2025 AutoCare Platform. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFname() != null ? user.getFname() : "",
                user.getLname() != null ? user.getLname() : "",
                user.getUsername() != null ? user.getUsername() : "",
                user.getcName() != null ? user.getcName() : "N/A"
        );
    }

    /**
     * Build HTML content for rejection email
     */
    private String buildRejectionEmailContent(User user) {
        String reason = user.getRejectionReason() != null ?
                user.getRejectionReason() :
                "Your registration did not meet our current requirements.";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
                    .button { display: inline-block; padding: 12px 30px; background: #3b82f6; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .reason-box { background: #fef2f2; border-left: 4px solid #ef4444; 
                                 padding: 15px; margin: 20px 0; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Registration Status Update</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s %s,</p>
                        
                        <p>Thank you for your interest in AutoCare Platform.</p>
                        
                        <p>After careful review, we regret to inform you that we are unable to approve 
                           your registration at this time.</p>
                        
                        <div class="reason-box">
                            <strong>Reason:</strong><br>
                            %s
                        </div>
                        
                        <p><strong>What you can do:</strong></p>
                        <ul>
                            <li>Review our registration requirements</li>
                            <li>Ensure all information provided is accurate and complete</li>
                            <li>Contact our support team for clarification</li>
                            <li>Re-apply with updated information</li>
                        </ul>
                        
                        <div style="text-align: center;">
                            <a href="http://localhost:3000/signup" class="button">Register Again</a>
                        </div>
                        
                        <p>If you believe this decision was made in error or have questions, 
                           please contact our support team at support@autocare.com</p>
                        
                        <p>Best regards,<br>
                        <strong>AutoCare Platform Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>&copy; 2025 AutoCare Platform. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFname() != null ? user.getFname() : "",
                user.getLname() != null ? user.getLname() : "",
                reason
        );
    }
}