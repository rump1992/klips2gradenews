package com.rump.klips2gradenews;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailService {

  public void sendInfoMail(String username) {
    String to = String.format("%s@smail.uni-koeln.de", username);
    String from = "Klips2GradeNews";
    String host = "localhost";
    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", host);
    Session session = Session.getDefaultInstance(properties);

    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject("Neues Prüfungsergebnis!");
      message.setText(String.format(
          "Hey %s, \nes wird spannend! Ein neues Prüfungsergebnis wurde hinterlegt!\nViel Glück!\n\nMit besten Grüßen\nKlips2GradeNews",
          username));
      Transport.send(message);
    } catch (MessagingException e) {
      // TODO: Logging
    }
  }

}
