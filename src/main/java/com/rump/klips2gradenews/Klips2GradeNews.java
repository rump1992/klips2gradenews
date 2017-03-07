package com.rump.klips2gradenews;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Klips2GradeNews {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final Logger logger = LogManager.getLogger();
  private static final WebScraper scraper = new WebScraper();

  public static void main(String[] args) {
    System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver");
    Klips2GradeNews gradeNews = new Klips2GradeNews();
    if (gradeNews.parseArgs(args)) {
      scraper.init();
      gradeNews.start();
    }
  }

  private void check() {
    logger.debug("webscraper starts checking for new exam results");
    scraper.checkForNewTestResults();
  }

  private void start() {
    final Runnable checkRoutine = () -> {
      try {
        check();
      } catch (Exception e) {
        logger.error("Problem in check routine, perhaps Klips2 has been changed\nStacktrace:\n", e);
      }
    };

    scheduler.scheduleAtFixedRate(checkRoutine, 0, 10, TimeUnit.MINUTES);
    logger.debug("check routine started");
  }

  private Options getOptions() {
    Option SSLOnConnect =
        new Option("ssl", "SSLOnConnect", false, "SSL is used when connecting to SMTP server");

    Option username = Option.builder("u").longOpt("username").desc("your KLIPS2 username").hasArg()
        .argName("USERNAME").required().build();
    Option password = Option.builder("p").longOpt("password").desc("your KLIPS2 password").hasArg()
        .argName("PASSWORD").required().build();
    Option emailAddress = Option.builder("ea").longOpt("email_address")
        .desc("email address from which the news are sent").hasArg().argName("EMAIL_ADDRESS")
        .required().build();
    Option emailUsername = Option.builder("eu").longOpt("email_username")
        .desc("username for above mentioned email address").hasArg().argName("EMAIL_USERNAME")
        .required().build();
    Option emailPassword = Option.builder("ep").longOpt("email_password")
        .desc("password for above mentioned email address").hasArg().argName("EMAIL_PASSWORD")
        .required().build();
    Option hostName = Option.builder("ehn").longOpt("email_host_name")
        .desc("host name of the email address' provider, e.g. 'mail.gmx.net' for GMX").hasArg()
        .argName("HOST_NAME").required().build();
    Option smtpPort =
        Option.builder("sp").longOpt("smtp_port").desc("SMTP port of the email address' provider")
            .hasArg().argName("SMTP_PORT").required().build();

    Options options = new Options();
    options.addOption(SSLOnConnect);
    options.addOption(username);
    options.addOption(password);
    options.addOption(emailAddress);
    options.addOption(emailUsername);
    options.addOption(emailPassword);
    options.addOption(hostName);
    options.addOption(smtpPort);

    return options;
  }

  private boolean parseArgs(String[] args) {
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    Options options = getOptions();
    try {
      CommandLine line = parser.parse(options, args);
      scraper.setCredentials(line.getOptionValue("u"), line.getOptionValue("p"));
      MailService mailService = new MailService();
      mailService.setEmailAddress(line.getOptionValue("ea"));
      mailService.setPassword(line.getOptionValue("ep"));
      mailService.setUsername(line.getOptionValue("eu"));
      mailService.setSSLOnConnect(line.hasOption("ssl"));
      mailService.setsmtpPort(Integer.parseInt(line.getOptionValue("sp")));
      mailService.setHostName(line.getOptionValue("ehn"));
      scraper.setMailService(mailService);
      return true;

    } catch (ParseException e) {
      formatter.printHelp("Klips2GradeNews",
          "Sends automatically an email if you have new grades in Klips2", options,
          "Please report issues to dennis_rump@gmx.de", true);
      return false;
    }
  }
}
