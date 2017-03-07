package com.rump.klips2gradenews;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebScraper {
  Logger logger = LogManager.getLogger();
  private final String URL = "https://klips2.uni-koeln.de/co/webnav.ini";
  private String username;
  private String password;
  private int numberOfTestResultsTableCells;
  private IMailService mailService;

  @SuppressWarnings("unchecked")
  public void checkForNewTestResults() {
    int numberOfCells = getTestResultsTableCellsSize();

    if (numberOfCells > numberOfTestResultsTableCells) {
      mailService.sendInfoMail(username);
      numberOfTestResultsTableCells = numberOfCells;
      logger.info("number of table cells updated");
      return;
    }
    logger.info("no new exam results");
  }

  private int getTestResultsTableCellsSize() {
    FirefoxProfile profile = new FirefoxProfile();
    DesiredCapabilities caps =
        new FirefoxOptions().setProfile(profile).addTo(DesiredCapabilities.firefox());

    WebDriver driver = new FirefoxDriver(caps);
    driver.get(URL);
    driver.switchTo().frame(driver.findElement(By.name("menue")));
    WebElement login = driver.findElement(By.id("menue_frame_key_icon"));
    login.click();
    (new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {

      @Override
      public Boolean apply(WebDriver d) {
        return d.getTitle().equals("Anmeldung - KLIPS 2.0 - Universität zu Köln");
      }
    });
    driver.switchTo().parentFrame();
    driver.switchTo().frame(driver.findElement(By.name("detail")));
    WebElement usernameInput = driver.findElement(By.name("cp1"));
    usernameInput.sendKeys(username);
    WebElement passwordInput = driver.findElement(By.name("cp2"));
    passwordInput.sendKeys(password);
    WebElement loginButton = driver.findElement(By.name("pAction"));
    loginButton.click();
    (new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {

      @Override
      public Boolean apply(WebDriver d) {
        return d.getTitle().startsWith("Informationen - ")
            || d.getTitle().startsWith("Visitenkarte von");
      }
    });

    if (driver.getTitle().startsWith("Informationen - ")) {
      WebElement ff = driver.findElement(By.id("ff"));
      ff.click();
    }

    (new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {

      @Override
      public Boolean apply(WebDriver d) {
        return d.getTitle().startsWith("Visitenkarte von");
      }
    });

    WebElement testResultsLink = driver.findElement(By.linkText("Prüfungsergebnisse"));
    testResultsLink.click();

    (new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {

      @Override
      public Boolean apply(WebDriver d) {
        return d.getTitle().startsWith("Prüfungsergebnisse");
      }
    });

    List<WebElement> listTables = driver.findElements(By.className("list"));
    WebElement testResultsTable = listTables.get(listTables.size() - 1);
    List<WebElement> tableCells = testResultsTable.findElements(By.tagName("TD"));
    int numberOfCells = tableCells.size();
    driver.quit();

    return numberOfCells;
  }

  public void setCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public void setMailService(IMailService mailService) {
    this.mailService = mailService;
  }

  public void init() {
    numberOfTestResultsTableCells = getTestResultsTableCellsSize();
  }
}
