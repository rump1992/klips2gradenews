package com.rump.klips2gradenews;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Run {
  public static void main(String[] args) {
    Optional<CommandLine> line = parseArgs(args);
    if (line.isPresent()) {
      IMailService mailService = initMailService(line.get());
      WebScraper webScraper = initWebScraper(line.get());
      Checker checker = new Checker(webScraper, mailService, 10, TimeUnit.MINUTES);
      checker.start();
    }
  }

  private static Optional<CommandLine> parseArgs(String[] args) {
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    Options options = getOptions();
    try {
      CommandLine line = parser.parse(options, args);
      return Optional.of(line);

    } catch (ParseException e) {
      formatter.printHelp("Klips2GradeNews",
          "Sends automatically an email if you have new grades in Klips2", options,
          "Please report issues to dennis_rump@gmx.de", true);
      return Optional.empty();
    }
  }

  private static Options getOptions() {
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

  private static IMailService initMailService(CommandLine line) {
    MailService mailService = new MailService(line.getOptionValue("eu"), line.getOptionValue("ep"),
        line.getOptionValue("u"), line.getOptionValue("ea"),
        Integer.parseInt(line.getOptionValue("sp")), line.hasOption("ssl"),
        line.getOptionValue("ehn"));

    return mailService;
  }

  private static WebScraper initWebScraper(CommandLine line) {
    WebScraper scraper = new WebScraper(line.getOptionValue("u"), line.getOptionValue("p"));
    return scraper;
  }
}
