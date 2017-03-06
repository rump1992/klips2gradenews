package com.rump.klips2gradenews;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

  private final String URL = "https://klips2.uni-koeln.de/co/webnav.ini";

  @SuppressWarnings("unchecked")
  public void checkForNewTestResults(String username, String password) {
    DesiredCapabilities caps =
        new FirefoxOptions().setProfile(new FirefoxProfile()).addTo(DesiredCapabilities.firefox());
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
    if (tableCells.size() == 1) {
      if (tableCells.get(0).getText().equals("Keine Teilbeurteilungen vorhanden"))
        return;
    }

    JSONParser parser = new JSONParser();

    try {
      Object object = parser.parse(new FileReader("storage.json"));
      JSONObject jsonObject = (JSONObject) object;
      Integer numberOfCells = (Integer) jsonObject.get("NumberOfCells");
      if (tableCells.size() > numberOfCells) {
        (new MailService()).sendInfoMail(username);
        jsonObject.put("NumberOfCells", tableCells.size());
        try (FileWriter file = new FileWriter("storage.json")) {
          file.write(jsonObject.toJSONString());
          file.flush();
        } catch (IOException e) {
          // TODO:Logging
        }
      }
    } catch (IOException | ParseException ex) {
      // TODO: logging
    }


  }
}
