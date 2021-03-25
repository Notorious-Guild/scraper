package Scraper.Util

import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

class HttpUtil {
  static CloseableHttpClient httpClient() {
    def timeout = System.getenv("HTTP_TIMEOUT") as Integer ?: 10000
    RequestConfig cfg = RequestConfig.custom()
        .setConnectTimeout(timeout)
        .setConnectionRequestTimeout(timeout)
        .setSocketTimeout(timeout)
        .build()
    return HttpClientBuilder.create().setDefaultRequestConfig(cfg).build()
  }
}
