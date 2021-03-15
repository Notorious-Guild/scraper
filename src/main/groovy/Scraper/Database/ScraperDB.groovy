package Scraper.Database

import Scraper.Models.Player
import java.sql.*

class ScraperDB {

  private Connection connection

  // verifyServerCertificate=false&useSSL=true&allowMultiQueries=true
  ScraperDB(String url, String username, String password) {
    connection = DriverManager.getConnection(url, username, password)
  }

  List<Player> getPlayers() {
    List<Player> players = new ArrayList<Player>()
    String sql = "SELECT `name`,`server_code`,`region` FROM `player`"
    List playerResults = get(sql)
    playerResults.each { playerMap ->
      players.add(new Player(playerMap))
    }
    return players
  }

  Boolean playerExists(Player player) {
    String sql = "SELECT * FROM `player` WHERE `name` = ?"
    List playerResults = get(sql, [player.name])
    return playerResults.size() > 0
  }

  void insertPlayer(Player player) {
    String sql = "INSERT INTO `player` VALUES(0,?,?,?)"
    set(sql, [player.name, player.getServerCode(), "us"])
  }

  private List<Map> get(String sql, List parameters = []) {
    List<Map> output = new ArrayList<Map>()

    PreparedStatement statement = connection.prepareStatement(sql)

    parameters.eachWithIndex { parameter, index ->
      statement.setObject(index+1, parameter)
    }

    ResultSet resultSet = statement.executeQuery()
    ResultSetMetaData metaData = resultSet.getMetaData()

    while (resultSet.next()) {
      Map result = new LinkedHashMap()
      for (def i = 1 ; i < metaData.getColumnCount() ; i++) {
        result.put(metaData.getColumnName(i), resultSet.getObject(i))
      }
      output.push(result)
    }
    return output
  }

  private void set(String sql, List parameters = []) {
    PreparedStatement statement = connection.prepareStatement(sql)
    parameters.eachWithIndex { parameter, index ->
      statement.setObject(index+1, parameter)
    }
    statement.executeQuery()
  }

  void closeConnection() {
    connection.close()
  }
}
