package Scraper.Util

import groovy.util.logging.Slf4j
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicNameValuePair

import java.nio.charset.StandardCharsets

@Slf4j
class Helpers {

  static boolean updateWowProgressProfile(String character, String realm) {
    def pairs = [
      new BasicNameValuePair('update', '1')
    ]
    def entity = new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8)

    HttpPost request = new HttpPost("$Constants.WOWPROG_URL/character/us/$realm/$character")
    request.setHeader('Content-Type', 'application/x-www-form-urlencoded')
    request.setHeader('Accept', 'application/json')
    request.setEntity(entity)

    CloseableHttpClient httpClient = HttpUtil.httpClient()
    CloseableHttpResponse response = httpClient.execute(request)

    int statusCode = response.getStatusLine().getStatusCode()

    if (statusCode != 200) {
      log.warn('WowProgress profile update failed.')
      return false
    } else {
      log.info('WowProgress profile updated successfully.')
      return true
    }
  }
}
