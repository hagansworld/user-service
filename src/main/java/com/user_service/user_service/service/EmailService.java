package com.user_service.user_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(String to, String subject, String text){

        try{
            log.info("preparing to send email {}", to);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text,true);

            javaMailSender.send(message);
            log.info("verification email sent successfully {}", message);
        } catch (MessagingException e) {
            log.error("Failed to send verification {}" ,e.getMessage());
            throw new RuntimeException(e);
        }

    }


}
