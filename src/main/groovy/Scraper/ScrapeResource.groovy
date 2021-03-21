package Scraper

import Scraper.BNET.BattlenetRequest
import Scraper.Pages.WoWProgress.LfgPage
import Scraper.Pages.WoWProgress.WoWProgPage
import Scraper.WCL.WCLRequest
import Scraper.WCL.ProfileRequest
import Scraper.Database.ScraperDB
import Scraper.Discord.EmbedRequest
import Scraper.Models.Player
import Scraper.Util.HttpUtil
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

@Slf4j
class ScrapeResource {

  static final String TARGET_REGION = "us"
  static final Double PERFORMANCE_FILTER = 80.00
  static final Double ITEM_LEVEL_FILTER = 220.00
  static final int MIN_PROGRESS = 8

  ScrapeResource() {

    String url = System.getenv("DB_URL") as String
    ScraperDB database = new ScraperDB(url)

    HttpClient httpClient = HttpUtil.httpClient()

    WCLRequest profileRequest = new ProfileRequest()
    BattlenetRequest bnetRequest = new BattlenetRequest()

    // Get Players from WoW Prog
    WoWProgPage playerPage = new LfgPage()
    List<Player> players = playerPage.getPlayers(TARGET_REGION, ITEM_LEVEL_FILTER)
    List<Player> invalidPlayers = new ArrayList<Player>()
    List<String> addedPlayers = []

    // update players data
    players.each {Player player ->
      if (!profileRequest.update(player)) {
        invalidPlayers.add(player)
      }
    }

    // remove each player flagged as invalid
    invalidPlayers.each { Player player ->
      players.remove(player)
    }

    // filters
    players.removeAll { Player player ->
      int currentMythicProg = player.getProgress().split("/")[0] as Integer

      return currentMythicProg < MIN_PROGRESS
        || player.getAvgPerformance() < PERFORMANCE_FILTER
        || database.playerExists(player)
    }

    // update IO
    players.each { player ->

      log.info("Player $player.name processing...")

      player.setAvatarUrl(bnetRequest.getAvatarUri(player.getName().toLowerCase(), player.getServerSlug()))

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

        if (response) {
          Map responseMap = new JsonSlurper().parseText(response) as Map

          List<Double> mythicPlusScores = (responseMap.mythic_plus_scores_by_season as List<Map>)
              .collect({ (it.scores as Map).all }) as List<Double>

          player.setCurrentIO(mythicPlusScores[0])
          if (mythicPlusScores.size() > 0) {
            player.setPreviousIO(mythicPlusScores[1])
          }
        }
      } catch (JsonException ignored) {
        log.error('Error parsing raider.io data')
      } catch (Exception ignored) {
      }
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
    if (hour < 10) {
      hour = "0$hour"
    }
    if (min < 10) {
      min = "0$min"
    }
    if (second < 10) {
      second = "0$second"
    }

    def date = "${dt.getYear()}-${dt.getMonthOfYear()}-${dt.getDayOfMonth()}"
    def time = "${hour}:${min}:${second}"

    def timeStamp = "${date}T${time}.000Z"

    players.each { player ->

      Map links = [
          warcraftLogs: "https://www.warcraftlogs.com/character/us/$player.serverSlug/$player.name#difficulty=5",
          wowProgress : "https://www.wowprogress.com/character/us/$player.serverSlug/$player.name"
      ]

      EmbedRequest request = new EmbedRequest(player, links, timeStamp)

      post.setEntity(request.toEntity())
      try {
        httpClient.execute(post)
        database.insertPlayer(player)
        addedPlayers.add(player.getName())
      } catch (ConnectionPoolTimeoutException ignored) {
        log.info("Player $player.name unable to post to discord.")
      }
    }

    database.closeConnection()
    log.info("${addedPlayers.size()} player(s) were added from this process")
  }
}
