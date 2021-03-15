package Scraper

import Scraper.Database.ScraperDB
import Scraper.Discord.EmbedRequest
import Scraper.Models.Player
import Scraper.Pages.WarcraftLogs.ParsePage
import Scraper.Pages.WarcraftLogs.WarcraftLogsPage
import Scraper.Pages.WoWProgress.LfgPage
import Scraper.Pages.WoWProgress.PlayerPage
import Scraper.Pages.WoWProgress.WoWProgPage
import Scraper.Util.ClassColors
import Scraper.Util.PageScraper
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ConnectionPoolTimeoutException
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.openqa.selenium.remote.RemoteWebDriver

@Slf4j
class ScrapeResource {

  static final String TARGET_REGION = "us"
  static final Double PERFORMANCE_FILTER = 80.00
  static final Double ITEM_LEVEL_FILTER = 210.00
  static final int MIN_PROGRESS = 6

  ScrapeResource() {

    String url = System.getenv("DB_URL") as String
    String username = System.getenv("DB_USERNAME") as String
    String password = System.getenv("DB_PASSWORD") as String
    ScraperDB database = new ScraperDB(url, username, password)

    PageScraper scraper = new PageScraper()
    RemoteWebDriver driver = scraper.getDriver()
    HttpClient httpClient = scraper.httpClient()

    // Get Players from WoW Prog
    WoWProgPage playerPage = new LfgPage()
    driver.navigate().to(playerPage.pageUrl)
    List<Player> players = playerPage.getPlayers(driver, TARGET_REGION, ITEM_LEVEL_FILTER)

    Integer playersAdded = 0

    players.each { player ->

      if(database.playerExists(player)) {
        return
      }

      log.info("Player $player.name processing...")

      // Player Info
      WoWProgPage playerInfo = new PlayerPage(player.name, TARGET_REGION, player.getServerCode())
      driver.navigate().to(playerInfo.pageUrl)
      player.setBio(playerInfo.getBio(driver))
      player.setRaidsPerWeek(playerInfo.getRaidsPerWeek(driver))
      player.setBattleTag(playerInfo.getBattleTag(driver))
      player.setPlayerClass(playerInfo.getPlayerClass(driver))
      player.setClassColor(ClassColors.get(player.playerClass.trim().toLowerCase()))

      // Player Parses
      WarcraftLogsPage parsePage = new ParsePage(player.name, TARGET_REGION, player.getServerCode())
      driver.navigate().to(parsePage.pageUrl)
      player.setAvatarUrl(parsePage.getAvatarURL(driver))
      player.setSpecIconUrl(parsePage.getSpecIcon(driver))
      player.setProgress(parsePage.getProgress(driver))
      player.setAvgPerformance(parsePage.getAverageParse(driver) as Double)
      player.setMedianPerformance(parsePage.getMedianPerformance(driver) as Double)

      if ((player.getProgress().split("/")[0] as Integer) < MIN_PROGRESS) {
        return
      }

      // Raider.io Info
      URI uri = new URIBuilder('https://raider.io/api/v1/characters/profile')
        .addParameter('region', TARGET_REGION)
        .addParameter('realm', player.serverCode)
        .addParameter('name', player.name)
        .addParameter('fields', 'mythic_plus_scores_by_season:current:previous')
        .build()

      HttpGet get = new HttpGet(uri)

      try {
        String response = httpClient.execute(get)?.getEntity()?.getContent()?.getText()

        if(response) {
          Map responseMap = new JsonSlurper().parseText(response) as Map

          List<Double> mythicPlusScores = (responseMap.mythic_plus_scores_by_season as List<Map>)
            .collect({(it.scores as Map).all}) as List<Double>

          player.setCurrentIO(mythicPlusScores[0])
          if(mythicPlusScores.size() > 0) {
            player.setPreviousIO(mythicPlusScores[1])
          }
        }
      } catch(JsonException ignored) {
        log.error('Error parsing raider.io data')
      } catch(Exception ignored) {}
    }

    driver.quit()

    players.removeAll {player ->
      player.avgPerformance < PERFORMANCE_FILTER
    }

    if (players.size() < 1) {
      log.info("No new players found")
      return
    }

    def hook = System.getenv("DISCORD_WEBHOOK")
    HttpPost post = new HttpPost(hook)
    post.setHeader("Content-Type", "application/json")

    DateTime dt = DateTime.now(DateTimeZone.UTC)

    def hour = dt.getHourOfDay()
    def min = dt.getMinuteOfHour()
    def second = dt.getSecondOfMinute()
    if(hour < 10) {hour="0$hour"}
    if(min < 10) {min="0$min"}
    if(second < 10) {second="0$second"}

    def date = "${dt.getYear()}-${dt.getMonthOfYear()}-${dt.getDayOfMonth()}"
    def time = "${hour}:${min}:${second}"

    def timeStamp = "${date}T${time}.000Z"

    players.each {player ->

      Map links = [
        warcraftLogs: (new ParsePage(player.name, TARGET_REGION, player.getServerCode())).pageUrl,
        wowProgress: new PlayerPage(player.name, TARGET_REGION, player.getServerCode()).pageUrl
      ]

      EmbedRequest request = new EmbedRequest(player, links, timeStamp)

      post.setEntity(request.toEntity())
      try {
        httpClient.execute(post)
        database.insertPlayer(player)
        playersAdded++
      } catch (ConnectionPoolTimeoutException ignored) {
        log.info("Player $player.name unable to post to discord.")
      }
    }

    database.closeConnection()
    log.info("$playersAdded player(s) were added from this process")
  }
}
