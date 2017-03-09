package com.rump.klips2gradenews;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Checker {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final Logger logger = LogManager.getLogger();
  private WebScraper scraper;
  private IMailService mailService;
  private long period;
  private TimeUnit unit;

  public Checker(WebScraper scraper, IMailService mailService, long period, TimeUnit unit) {
    this.scraper = checkNotNull(scraper);
    this.mailService = checkNotNull(mailService);
    this.period = checkNotNull(period);
    this.unit = checkNotNull(unit);
  }

  private void check() {
    logger.debug("webscraper starts checking for new exam results");
    if (scraper.hasKlips2NewExamResults())
      mailService.sendInfoMail();
  }

  public void start() {
    final Runnable checkRoutine = () -> {
      try {
        check();
      } catch (Exception e) {
        logger.error("problem in check routine, perhaps Klips2 has been changed", e);
      }
    };

    scheduler.scheduleAtFixedRate(checkRoutine, 0, period, unit);
    logger.debug("check routine started");
  }
}
