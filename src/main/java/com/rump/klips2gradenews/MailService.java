package com.rump.klips2gradenews;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailService implements IMailService {
  Logger logger = LogManager.getLogger();
  private String username;
  private String password;
  private String emailAddress;
  private String hostName;
  private int smtpPort;
  private boolean SSLOnConnect;

  public void sendInfoMail(String klips2username) {
    Email email = new SimpleEmail();
    email.setHostName(hostName);
    email.setSmtpPort(smtpPort);
    email.setAuthenticator(new DefaultAuthenticator(username, password));
    email.setSSLOnConnect(SSLOnConnect);
    try {
      email.setFrom(emailAddress);
    } catch (EmailException e) {
      logger.error("Invalid email address", e);
    }
    email.setSubject("Neue Prüfungsergebnisse!");
    try {
      email.setMsg(String.format(
          "Hi %s,\n\ndu hast ein neues Prüfungsergebnis in Klips2!\nViel Glück!\n\nMit besten Grüßen\nKlips2GradeNews",
          klips2username));
    } catch (EmailException e) {
      logger.error("Invalid message", e);
    }
    try {
      email.addTo(String.format("%s@smail.uni-koeln.de", klips2username));
    } catch (EmailException e) {
      logger.error("Invalid recipient", e);
    }
    try {
      email.send();
      logger.info("info mail sent");
    } catch (EmailException e) {
      logger.error("error at sending email", e);
    }
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public void setSSLOnConnect(boolean SLLOnConnect) {
    this.SSLOnConnect = SLLOnConnect;
  }

  public void setsmtpPort(int smtpPort) {
    this.smtpPort = smtpPort;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }
}
