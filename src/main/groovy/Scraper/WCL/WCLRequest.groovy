package Scraper.WCL

import Scraper.Models.Player
import Scraper.Util.ClassColors
import Scraper.Util.HttpUtil
import Scraper.WCL.Queries.GetPlayerData

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat

@Slf4j
abstract class WCLRequest {
  private String api_uri = "https://www.warcraftlogs.com"
  private String icon_uri = "https://assets.rpglogs.com/img/warcraft/icons"
  private httpClient = HttpUtil.httpClient()
  private String accessToken

  private JsonOutput jsonOutput = new JsonOutput()
  private JsonSlurper jsonSlurper = new JsonSlurper()

  WCLRequest() {
    accessToken = generateAccessToken()
  }

  protected Player requestPlayerData(String playerName, String serverName, Player player = new Player()) {
    HttpPost get = new HttpPost("$api_uri/api/v2/client")
    get.setHeader("Authorization", "Bearer $accessToken")
    get.setHeader("Content-Type", "application/json")
    Map variables = new LinkedHashMap<String, String>()
    variables.put("name", playerName)
    variables.put("server", serverName)

    String query = new GetPlayerData() as String

    Map requestVars = new LinkedHashMap<String, Object>()
    requestVars.put("variables", variables)
    requestVars.put("query", query)
    get.setEntity(new StringEntity(jsonOutput.toJson(requestVars)))
    def response = httpClient.execute(get)

    Map responseObject = jsonSlurper.parseText(EntityUtils.toString(response.getEntity())) as Map
    int responseCode = response.getStatusLine().getStatusCode()
    EntityUtils.consume(response.getEntity())

    if (responseCode != 200) {
      throw new Exception("Warcraftlogs responded with a non-200 status code.")
    }

    if (!responseObject["data"]["player"]["character"]) {
      log.error("Character $playerName-$serverName does not exist on Warcraftlogs.")
      return null
    }

    if (responseObject["data"]["player"]["character"]["gameData"]["error"]) {
      log.error("Character $playerName-$serverName has an out of date Warcraftlogs profile. Will attempt to update.")
      updateWarcraftlogsProfile(responseObject['data']['player']['character']['canonicalID'] as String)
      return null
    }

    Map zoneRankings = responseObject["data"]["player"]["character"]["zoneRankings"] as Map
    Map characterData = responseObject["data"]["player"]["character"]["gameData"]["global"] as Map
    player.setName(characterData["name"] as String)
    player.setServer(characterData["realm"]["name"] as String)
    player.setServerSlug(characterData["realm"]["slug"] as String)
    player.setPlayerClass(characterData["character_class"]["name"] as String)
    player.setSpec(characterData["active_spec"]["name"] as String)
    def urlClass = player.getPlayerClass().toLowerCase().replaceAll(" ", new String())
    def urlSpec = player.getSpec().toLowerCase().replaceAll(" ", new String())
    player.setSpecIconUrl("$icon_uri/$urlClass-${urlSpec}.jpg")
    player.setClassColor(ClassColors.get(player.getPlayerClass().toLowerCase()))
    if (zoneRankings["difficulty"] == 5) {
      int prog = 0
      zoneRankings["rankings"].each { it["rankPercent"] ? prog++ : prog }
      player.setProgress("$prog/10")
    } else {
      player.setProgress("0/10")
    }

    DecimalFormat df = new DecimalFormat("#.##")
    if (zoneRankings['bestPerformanceAverage']) {
      player.setAvgPerformance(df.format(zoneRankings['bestPerformanceAverage']) as Double)
    }
    if (zoneRankings['medianPerformanceAverage']) {
      player.setMedianPerformance(df.format(zoneRankings["medianPerformanceAverage"]) as Double)
    }

    return player
  }

  private String generateAccessToken() {
    def wcl_secret = System.getenv("WCL_SECRET")
    byte[] encodedAuth = Base64.encoder.encode(wcl_secret.getBytes(StandardCharsets.ISO_8859_1))
    String authHeader = "Basic " + new String(encodedAuth)

    HttpPost request = new HttpPost("$api_uri/oauth/token")
    request.setHeader(HttpHeaders.AUTHORIZATION, authHeader)
    request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    request.setEntity(new StringEntity(jsonOutput.toJson([grant_type:"client_credentials"])))

    CloseableHttpResponse response = httpClient.execute(request)
    int statusCode = response.getStatusLine().getStatusCode()

    if (statusCode != 200) {
      return null
    } else {
      def token = (jsonSlurper.parseText(response.getEntity().getContent().getText()) as Map)?.access_token
      EntityUtils.consume(response.getEntity())
      return token
    }
  }

  private updateWarcraftlogsProfile(String canonicalID) {
    HttpGet request = new HttpGet("$api_uri/character/update/$canonicalID")

    CloseableHttpResponse response = httpClient.execute(request)
    int statusCode = response.getStatusLine().getStatusCode()

    if (statusCode != 200) {
      log.warn('Warcraftlogs profile update failed.')
    } else {
      log.info('Warcraftlogs profile updated successfully.')
    }

    response.close()
  }
}
