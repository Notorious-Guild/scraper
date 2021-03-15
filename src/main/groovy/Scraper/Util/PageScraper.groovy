package Scraper.Util

import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.openqa.selenium.firefox.FirefoxBinary
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxDriverLogLevel
import org.openqa.selenium.firefox.FirefoxOptions

class PageScraper {

  private FirefoxBinary firefoxBinary
  private FirefoxOptions firefoxOptions
  private FirefoxDriver driver

  PageScraper() {
    firefoxBinary = new FirefoxBinary()
    firefoxBinary.addCommandLineOptions("--headless")

    System.setProperty("webdriver.gecko.driver", "geckodriver.exe")
    System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");

    firefoxOptions = new FirefoxOptions()
    firefoxOptions.setBinary(firefoxBinary)
    firefoxOptions.setLogLevel(FirefoxDriverLogLevel.FATAL)

    driver = new FirefoxDriver(firefoxOptions)

    //WebDriverWait wait = new WebDriverWait(driver, 30)
  }

  FirefoxDriver getDriver() {
    return driver
  }

  HttpClient httpClient() {
    def timeout = System.getenv("HTTP_TIMEOUT") as Integer
    RequestConfig cfg = RequestConfig.custom()
        .setConnectTimeout(timeout)
        .setConnectionRequestTimeout(timeout)
        .setSocketTimeout(timeout)
        .build()
    return HttpClientBuilder.create().setDefaultRequestConfig(cfg).build()
  }
}
