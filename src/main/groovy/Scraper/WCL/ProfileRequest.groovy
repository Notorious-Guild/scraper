package Scraper.WCL

import Scraper.Models.Player

class ProfileRequest extends WCLRequest {

  Player update(Player player) {
    if (requestPlayerData(player.getName(), player.getServerCode(), player)) {
      return player
    } else {
      return null
    }
  }
}
