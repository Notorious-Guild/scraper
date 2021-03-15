package Scraper.Pages.WoWProgress

import Scraper.Models.Player
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver

class LfgPage extends WoWProgPage {

  final static String LFG_TABLE_PLAYERS = '//*[@id="char_rating_container"]/table'

  LfgPage() {
    super("/gearscore/us?lfg=1&lang=en&sortby=ts")
  }

  /**
   * Gets Recent Players that have set an lfg status
   * @param RemoteWebDriver driver
   * @param String filterRegion
   * @return List<Player> players
   */
  List<Player> getPlayers(RemoteWebDriver driver, String filterRegion = "all", Double filterItemLevel = null) {

    WebElement table = driver.findElement(By.xpath(LFG_TABLE_PLAYERS))
    List<WebElement> rows = table.findElements(By.xpath(".//tr"))

    List<Player> players = new ArrayList<Player>()
    rows.each {
      try {
        Player player = new Player()

        Double itemLevel = it.findElements(By.xpath(".//td")).get(4).getText() as Double
        if(filterItemLevel && itemLevel >= filterItemLevel) {
          player.setName(it.findElements(By.xpath(".//td")).get(0).getText())
          player.setServer(it.findElements(By.xpath(".//td")).get(3).getText())
          player.setItemLevel(itemLevel)

          players.push(player)
        }
      } catch (Exception ignored) {}
    }
    return players
  }
}
