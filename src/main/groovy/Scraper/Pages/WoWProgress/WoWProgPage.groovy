package Scraper.Pages.WoWProgress

import Scraper.Pages.Page
import Scraper.Util.Constants

class WoWProgPage extends Page {

  WoWProgPage(String url) {
    pageUrl = Constants.WOWPROG_URL + url
  }
}
