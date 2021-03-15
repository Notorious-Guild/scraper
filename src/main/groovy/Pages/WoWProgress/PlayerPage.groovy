package Pages.WoWProgress

import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver

class PlayerPage extends WoWProgPage {

  static final String BIO_TEXT = '//*[@id="primary"]/div/div[2]/div[4]/div[11]'
  static final String RAIDS_PER_WEEK = '//*[@id="primary"]/div/div[2]/div[4]/div[8]/strong'
  static final String PLAYER_CLASS = '//*[@id="primary"]/div/div[2]/div[2]/div[1]/i/span'
  static final String BTAG_PATH = '//*[@id="primary"]/div/div[2]/div[4]/div[3]/span'

  PlayerPage(String playerName, String playerRegion, String playerServer) {
    super("/character/$playerRegion/$playerServer/$playerName")
  }

  /**
   * Gets Player BIO From the WoWProgress Player Info Page
   * @param RemoteWebDriver driver
   * @return String bio
   */
  String getBio(RemoteWebDriver driver) {
    try {
      return driver.findElements(By.xpath(BIO_TEXT)).get(0).getText()
    } catch (Exception ignored) {return new String()}
  }

  String getRaidsPerWeek(RemoteWebDriver driver) {
    try {
      String raidsPerWeek = driver.findElements(By.xpath(RAIDS_PER_WEEK)).get(0).getText()
      if (!raidsPerWeek.contains("-")) {
        return "Not Specified"
      }
      return raidsPerWeek
    } catch (Exception ignored) {return "Not Specified"}
  }

  String getPlayerClass(RemoteWebDriver driver) {
    try {
      String playerClass = driver.findElements(By.xpath(PLAYER_CLASS)).get(0).getText().toLowerCase().capitalize()
      return playerClass
    } catch (Exception ignored) {return "unknown"}
  }

  String getBattleTag(RemoteWebDriver driver) {
    try {
      return driver.findElements(By.xpath(BTAG_PATH)).get(0).getText()
    } catch (Exception ignored) {return "No Battletag Provided"}
  }
}
