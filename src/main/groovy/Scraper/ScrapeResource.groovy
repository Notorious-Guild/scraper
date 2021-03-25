package Scraper

import Scraper.BNET.BattlenetRequest
import Scraper.Pages.WoWProgress.LfgPage
import Scraper.Pages.WoWProgress.PlayerPage
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
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ConnectionPoolTimeoutException
import org.apache.http.util.EntityUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

@Slf4j
class ScrapeResource {

  static final Double PERFORMANCE_FILTER = System.getenv('PERFORMANCE_FILTER') as Double
  static final Double ITEM_LEVEL_FILTER = System.getenv('ITEM_LEVEL_FILTER') as Double
  static final int MIN_PROGRESS = System.getenv('MIN_PROGRESS') as Integer

  ScrapeResource() {

    String url = System.getenv("DB_URL") as String
    ScraperDB database = new ScraperDB(url)

    HttpClient httpClient = HttpUtil.httpClient()

    WCLRequest profileRequest = new ProfileRequest()
    BattlenetRequest bnetRequest = new BattlenetRequest()

    // Get Players from WoW Prog
    WoWProgPage lfgPage = new LfgPage()
    List<Player> players = new ArrayList<Player>()
    try {
      players = lfgPage.getPlayers(ITEM_LEVEL_FILTER)
    } catch (NullPointerException ignored) {
      log.error("There was an error on the wowprogress lfg page. please check $lfgPage.pageUrl")
      return
    }

    List<Player> invalidPlayers = new ArrayList<Player>()
    List<String> addedPlayers = []

    // remove existing players
    players.removeAll {Player player ->
      return database.playerExists(player)
    }

    // update basic player info
    players.each { Player player ->
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

    // update player data for player's that passed the filters
    players.each { player ->
      log.info("Player $player.name-$player.server processing...")

      WoWProgPage playerPage = new PlayerPage(player.name, player.serverSlug)

      player.setAvatarUrl(bnetRequest.getAvatarUri(player.getName().toLowerCase(), player.getServerSlug()))
      player.setBio(playerPage.getBio())
      player.setBattleTag(playerPage.getBattleTag())

      // Raider.io Info
      URI uri = new URIBuilder('https://raider.io/api/v1/characters/profile')
          .addParameter('region', 'us')
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

    // post players to discord
    players.each { player ->

      Map links = [
        warcraftLogs: "https://www.warcraftlogs.com/character/us/$player.serverSlug/$player.name#difficulty=5",
        wowProgress : "https://www.wowprogress.com/character/us/$player.serverSlug/$player.name"
      ]

      EmbedRequest request = new EmbedRequest(player, links, timeStamp)

      post.setEntity(request.toEntity())
      try {
        CloseableHttpResponse response = httpClient.execute(post)
        def code = response.getStatusLine().getStatusCode()
        EntityUtils.consume(response.getEntity())
        if (code != 204) {
          log.info("Player $player.name-$player.server unable to post to discord. (HTTP Status $code)")
          return
        }
        database.insertPlayer(player)
        addedPlayers.add(player.getName())
      } catch (ConnectionPoolTimeoutException ignored) {
        log.info("Player $player.name-$player.server unable to post to discord. (Timeout)")
        return
      }
      Thread.sleep(500)
    }

    database.closeConnection()
    log.info("${addedPlayers.size()} player(s) were added from this process")
  }
}
