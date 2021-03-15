package Pages.WarcraftLogs

import Pages.Page

class WarcraftLogsPage extends Page {

  final static String WCL_URL = "https://www.warcraftlogs.com"

  WarcraftLogsPage(String url) {
    pageUrl = WCL_URL + url
  }
}
