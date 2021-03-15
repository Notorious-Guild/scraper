package Pages.WarcraftLogs

import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver

class ParsePage extends WarcraftLogsPage {

  static final String PROGRESS_PATH = '//*[@id="raids-table-container"]/div[2]/div[1]/div[2]/div[2]/div/div[2]'
  static final String AVG_PARSE_PATH = '//*[@id="top-box"]/div[1]/div[1]/b'
  static final String MED_PARSE_PATH = '//*[@id="top-box"]/div[1]/div[2]/table/tbody/tr[1]/td[2]'
  static final String AVATAR_PATH = '//*[@id="character-portrait-image"]'
  static final String SPEC_ICON_PATH = '//*[@id="top-box"]/div[2]/div[2]/img'

  ParsePage(String playerName, String playerRegion, String playerServer) {
    super("/character/$playerRegion/$playerServer/$playerName#difficulty=5")
  }

  /**
   * Gets the player Progress for the current tier (x/y)
   * @param RemoveWebDriver driver
   * @return String progress
   */
  String getProgress(RemoteWebDriver driver) {
    try {
      return driver.findElements(By.xpath(PROGRESS_PATH)).get(0).getText().trim().replaceAll(" ", "")
    } catch (Exception ignored) {return "0/10"}
  }

  /**
   * Gets the Average Parse Performance from a Player
   * @param RemoteWebDriver driver
   * @return String averagePerformance
   */
  String getAverageParse(RemoteWebDriver driver) {
    try {
      def avg = driver.findElements(By.xpath(AVG_PARSE_PATH)).get(0).getText().trim()
      return avg != "-" ? avg : "0.00"
    } catch (Exception ignored) {return "0.00"}
  }

  String getMedianPerformance(RemoteWebDriver driver) {
    try {
      def medianParse = driver.findElements(By.xpath(MED_PARSE_PATH)).get(0).getText().trim().toLowerCase()
      return medianParse != "-" ? medianParse : "0.00"
    } catch (Exception ignored) {return "0.00"}
  }

  String getAvatarURL(RemoteWebDriver driver) {
    try {
      def avatarUrl = driver.findElements(By.xpath(AVATAR_PATH)).get(0).getAttribute("src")
      return avatarUrl
    } catch (Exception ignored) {return 'https://static.wikia.nocookie.net/wowpedia/images/a/a1/Orc_male.gif'}
  }

  String getSpecIcon(RemoteWebDriver driver) {
    try {
      def specUrl = driver.findElements(By.xpath(SPEC_ICON_PATH)).get(0).getAttribute("src")
      return specUrl
    } catch (Exception ignored) {return ''}
  }
}
