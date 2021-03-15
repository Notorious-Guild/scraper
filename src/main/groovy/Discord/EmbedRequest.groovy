package Discord

import Models.Player
import groovy.json.JsonOutput
import org.apache.http.entity.StringEntity

class EmbedRequest {

  Map request = new LinkedHashMap()

  EmbedRequest(Player player, Map links, String timeStamp) {
    request = [
      "embeds": [
        [
          "title": "A New Player is Looking for a Guild",
          "color": player.classColor,
          "author": [
            "name": "$player.name-$player.server",
            "url": links["warcraftLogs"],
            "icon_url": player.avatarUrl
          ],
          "description": "\n$player.bio\n",
          "fields": [
            [
              "name": "Battletag",
              "value": player.battleTag,
              "inline": false
            ],
            [
              "name": "Raids Per Week",
              "value": player.raidsPerWeek,
              "inline": false
            ],
            [
              "name": "Best Historic Average Parse",
              "value": "$player.avgPerformance%",
              "inline": false
            ],
            [
              "name": "Median Parse",
              "value": "$player.medianPerformance%",
              "inline": false
            ],
            [
              "name": "Raider.io Score (Current Season)",
              "value": "${player.currentIO == null ? 'No score found' : player.currentIO}",
              "inline": false
            ],
            [
              "name": "Raider.io Score (Previous Season)",
              "value": "${player.previousIO == null ? 'No score found' : player.previousIO}",
              "inline": false
            ],
            [
              "name": "Links",
              "value": "[WarcraftLogs](${links["warcraftLogs"]}) | [WoWProgress](${links["wowProgress"]}) | [WoWArmory](https://worldofwarcraft.com/en-us/character/us/${player.getServerCode()}/${player.name.toLowerCase()}) | [RaiderIO](https://raider.io/characters/us/${player.getServerCode()}/$player.name)",
              "inline": false
            ],
          ],
          "timestamp": timeStamp,
          "footer": [
            "text": "$player.spec $player.playerClass",
            "icon_url": player.specIconUrl
          ]
        ]
      ] as List
    ]
  }

  StringEntity toEntity() {
    return new StringEntity(this.toString())
  }

  @Override
  String toString() {
    return new JsonOutput().toJson(request)
  }
}
