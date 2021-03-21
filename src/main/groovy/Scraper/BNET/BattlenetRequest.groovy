package Scraper.BNET

import Scraper.Util.HttpUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets

class BattlenetRequest {
  private String api_uri = "https://us.api.blizzard.com"
  private String token_uri = "https://us.battle.net/oauth/token"
  private JsonSlurper jsonSlurper = new JsonSlurper()
  private JsonOutput jsonOutput = new JsonOutput()

  private String token

  private CloseableHttpClient httpClient = HttpUtil.httpClient()

  BattlenetRequest() {
    token = getToken()
  }

  String getAvatarUri(String playerName, String serverSlug) {
    URIBuilder builder = new URIBuilder("$api_uri/profile/wow/character/$serverSlug/$playerName/character-media")
    builder.addParameter("namespace", "profile-us")
    builder.addParameter("locale", "en_US")
    builder.addParameter("access_token", token)

    String requestUri = builder.build().toString()

    HttpGet get = new HttpGet(requestUri)
    CloseableHttpResponse response = httpClient.execute(get)

    Map responseMap = jsonSlurper.parseText(EntityUtils.toString(response.getEntity())) as Map
    EntityUtils.consume(response.getEntity())

    if (responseMap["code"] == 404) {
      return "https://static.wikia.nocookie.net/wowpedia/images/a/a1/Orc_male.gif"
    }

    return (responseMap["assets"] as List).get(0)["value"]
  }

  private String getToken() {
    String bnet_secret = System.getenv("BNET_SECRET")
    String authorization = bnet_secret.bytes.encodeBase64().toString()

    def pairs = []
    pairs.add(new BasicNameValuePair('grant_type', 'client_credentials'))

    def entity = new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8)

    def post = new HttpPost(token_uri)
    post.setHeader('Authorization', 'Basic ' + authorization)
    post.setHeader('Content-Type', 'application/x-www-form-urlencoded')
    post.setHeader('Accept', 'application/json')
    post.setEntity(entity)

    CloseableHttpResponse response = httpClient.execute(post)
    def responseMap = jsonSlurper.parseText(EntityUtils.toString(response.getEntity()))

    EntityUtils.consume(response.getEntity())

    return responseMap["access_token"] as String
  }
}
