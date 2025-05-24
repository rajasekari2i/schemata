package com.opsbeach.sharedlib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender javaMailSender;

    @Async
    public void sendMail(String toMailId, String subject, String messageBody){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toMailId);
        message.setSubject(subject);
        message.setText(messageBody);
        javaMailSender.send(message);
    }
}
