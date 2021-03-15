package Scraper.Pages.WoWProgress

import Scraper.Pages.Page

class WoWProgPage extends Page {

  final static String WOWPROG_URL = "https://www.wowprogress.com"

  WoWProgPage(String url) {
    pageUrl = WOWPROG_URL + url
  }
}
