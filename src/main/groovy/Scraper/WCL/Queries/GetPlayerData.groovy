package Scraper.WCL.Queries

class GetPlayerData {

  @Override String toString() {
    return """
    query getPlayer(\$name: String!, \$server: String!, \$region: String = "us") {
      player: characterData {
        character(name: \$name, serverSlug: \$server, serverRegion: \$region) {
          name
          classID
          gameData
          zoneRankings
          canonicalID
        }
      }
    }
    """
  }
}
