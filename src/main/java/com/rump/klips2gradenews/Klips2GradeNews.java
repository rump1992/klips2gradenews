package com.rump.klips2gradenews;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.json.simple.JSONObject;

public class Klips2GradeNews {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private String username;
  private String password;

  public static void main(String[] args) {
    Klips2GradeNews gradeNews = new Klips2GradeNews();
    gradeNews.initJSON();
    if (gradeNews.parseArgs(args))
      gradeNews.start();
  }

  private void check() {
    (new WebScraper()).checkForNewTestResults(username, password);
  }

  private void start() {
    final Runnable checkRoutine = () -> {
      // TODO:Logging
      try {
        check();
      } catch (Exception e) {
        e.printStackTrace();
      }
    };

    scheduler.scheduleAtFixedRate(checkRoutine, 0, 5, TimeUnit.MINUTES);
  }

  private Options getOptions() {
    Options options = new Options();
    options.addOption(Option.builder("u").longOpt("username").desc("Your username").hasArg()
        .argName("USERNAME").required().build());
    options.addOption(Option.builder("p").longOpt("password").desc("Your password").hasArg()
        .argName("PASSWORD").required().build());
    options.addOption(Option.builder("h").longOpt("help").build());

    return options;
  }

  private boolean parseArgs(String[] args) {
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    Options options = getOptions();
    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("h")) {
        formatter.printHelp("Klips2GradeNews",
            "Sends automatically an email if you have new grades in Klips2", options,
            "Please report issues to dennis_rump@gmx.de");
        return false;
      }
      this.username = line.getOptionValue("u");
      this.password = line.getOptionValue("p");
      return true;

    } catch (ParseException e) {
      formatter.printHelp("Klips2GradeNews",
          "Sends automatically an email if you have new grades in Klips2", options,
          "Please report issues to dennis_rump@gmx.de", true);
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private void initJSON() {
    File json = new File("storage.json");
    if (!json.exists()) {
      JSONObject object = new JSONObject();
      object.put("NumberOfCells", new Integer(0));

      try (FileWriter file = new FileWriter("storage.json")) {
        file.write(object.toJSONString());
        file.flush();
      } catch (IOException e) {
        // TODO:Logging
        e.printStackTrace();
      }
    }
  }
}
