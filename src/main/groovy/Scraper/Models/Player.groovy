package Scraper.Models

class Player {
  String name
  String server
  String serverSlug
  String playerClass
  String spec
  String battleTag
  String avatarUrl
  String specIconUrl

  String bio
  String raidsPerWeek
  String progress

  Double itemLevel
  Double avgPerformance
  Double medianPerformance
  Double currentIO
  Double previousIO

  Integer classColor

  Player(Map player) {
    this.name = player.name
    this.server = (player.server_code as String).replaceAll("-", " ").capitalize()
  }

  Player() {

  }

  String getServerCode() {
    (server.trim().toLowerCase()).replaceAll(" ", "-").replaceAll("'", "")
  }

  void setServer(String server) {
    this.server = (server as String).replaceAll("-", " ").capitalize()
  }

  String getServer() {
    return this.server
  }
}
