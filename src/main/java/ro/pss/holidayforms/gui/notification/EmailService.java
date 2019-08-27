package ro.pss.holidayforms.gui.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    @Autowired
    private JavaMailSender sender;
    public final boolean sendEmail(String targetUserEmail, String subject, String text) {
        try {
            SimpleMailMessage email = constructEmailMessage(targetUserEmail, subject, text);
            sender.send(email);
        }catch (MailException e) {
            log.error("sendEmail()", e);
            return false;
        }
        return true;
    }

    private final SimpleMailMessage constructEmailMessage(String targetUserEmail, String subject, String text) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(targetUserEmail);
        mail.setSubject(subject);
        mail.setText(text);
        return mail;
    }
}
