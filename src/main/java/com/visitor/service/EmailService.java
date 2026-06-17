package com.visitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendRegistrationConfirmation(String toEmail, String firstName, String lastName) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(toEmail);
			message.setSubject("Welcome to Visitor Management System");
			message.setText(String.format(
					"Dear %s %s,\n\n" +
							"Thank you for registering with us.\n" +
							"Your registration has been successfully completed.\n\n" +
							"We look forward to serving you.\n\n" +
							"Best regards,\n" +
							"Team",
							firstName, lastName
					));

			mailSender.send(message);
			log.info("Registration confirmation email sent to: {}", toEmail);
		} catch (Exception e) {
			log.error("Failed to send registration email to {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Email sending failed", e);
		}
	}

	public void sendInvitationEmail(String toEmail, String name, String invitationLink) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(toEmail);
			message.setSubject("Invitation to Join Visitor Management System");
			message.setText(String.format(
					"Dear %s,\n\n" +
							"You have been invited to join our team.\n\n" +
							"Your temporary credentials:\n" +
							"Email: %s\n" +
							"Please click the link below to accept your invitation and set your password:\n" +
							"%s\n\n" +
							"This invitation will expire in 48 hours.\n\n" +
							"Best regards,\n" +
							"Visitor Management Team",
							name, toEmail, invitationLink
					));

			mailSender.send(message);
			log.info("Invitation email sent to: {}", toEmail);
		} catch (Exception e) {
			log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Email sending failed", e);
		}
	}

	public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(toEmail);
			message.setSubject("Password Reset Request");
			message.setText(String.format(
					"Dear %s,\n\n" +
							"We received a request to reset your password.\n\n" +
							"Please click the link below to reset your password:\n" +
							"%s\n\n" +
							"This link will expire in 1 hour.\n\n" +
							"If you did not request a password reset, please ignore this email.\n\n" +
							"Best regards,\n" +
							"Visitor Management Team",
							name, resetLink
					));

			mailSender.send(message);
			log.info("Password reset email sent to: {}", toEmail);
		} catch (Exception e) {
			log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Email sending failed", e);
		}
	}
}
