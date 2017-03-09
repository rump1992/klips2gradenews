package com.rump.klips2gradenews;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailService implements IMailService {
  Logger logger = LogManager.getLogger();
  private String emailUsername;
  private String emailPassword;
  private String emailAddress;
  private String klips2Username;
  private String hostName;
  private int smtpPort;
  private boolean SSLOnConnect;

  public MailService(String emailUsername, String emailPassword, String klips2Username,
      String emailAddress, int smtpPort, boolean SSLOnConnect, String hostName) {
    this.emailUsername = checkNotNull(emailUsername);
    this.emailPassword = checkNotNull(emailPassword);
    this.emailAddress = checkNotNull(emailAddress);
    this.klips2Username = checkNotNull(klips2Username);
    this.smtpPort = checkNotNull(smtpPort);
    this.SSLOnConnect = checkNotNull(SSLOnConnect);
    this.hostName = checkNotNull(hostName);
  }

  public void sendInfoMail() {
    Email email = new SimpleEmail();
    email.setHostName(hostName);
    email.setSmtpPort(smtpPort);
    email.setAuthenticator(new DefaultAuthenticator(emailUsername, emailPassword));
    email.setSSLOnConnect(SSLOnConnect);
    try {
      email.setFrom(emailAddress, "Klips2GradeNews");
    } catch (EmailException e) {
      logger.error("invalid email address", e);
    }
    email.setSubject("Neue Prüfungsergebnisse!");
    try {
      email.setMsg(String.format(
          "Hi %s,\n\ndu hast ein neues Prüfungsergebnis in Klips2!\nViel Glück!\n\nMit besten Grüßen\nKlips2GradeNews",
          klips2Username));
    } catch (EmailException e) {
      logger.error("invalid message", e);
    }
    try {
      email.addTo(String.format("%s@smail.uni-koeln.de", klips2Username));
    } catch (EmailException e) {
      logger.error("invalid recipient", e);
    }
    try {
      email.send();
      logger.info("info mail sent");
    } catch (EmailException e) {
      logger.error("error at sending email", e);
    }
  }
}
