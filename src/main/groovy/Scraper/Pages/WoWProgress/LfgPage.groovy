package Scraper.Pages.WoWProgress

import Scraper.Models.Player
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList
import groovy.util.logging.Slf4j
import org.htmlcleaner.CleanerProperties
import org.htmlcleaner.DomSerializer
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import org.w3c.dom.Document

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.nio.charset.StandardCharsets

@Slf4j
class LfgPage extends WoWProgPage {

  final static String LFG_TABLE_PLAYERS = '//*[@id="char_rating_container"]/table'

  LfgPage() {
    super("/gearscore/us?lfg=1&lang=en&sortby=ts")
  }

  /**
   * Gets Recent Players that have set an lfg status
   * @param Double filterItemLevel
   * @return List<Player>  players
   */
  List<Player> getPlayers(Double filterItemLevel = 0.00) {
    def get = new URL(pageUrl).openConnection()
    def html = get.getInputStream().getText(StandardCharsets.UTF_8.name())

    TagNode tagNode = new HtmlCleaner().clean(html)
    Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode)

    XPath xpath = XPathFactory.newInstance().newXPath()
    DTMNodeList output = xpath.evaluate(LFG_TABLE_PLAYERS, doc, XPathConstants.NODESET)

    def table = output.item(0).getChildNodes().item(0).getChildNodes()

    List<Player> players = new ArrayList<Player>()

    (1..(table.getLength()-1)).each { int i ->
      def row = table.item(i).getChildNodes()
      if (row.getLength() == 6) {
        try {
          Player player = new Player()
          player.setName(row.item(0).getTextContent())
          player.setServer(row.item(3).getTextContent())
          player.setItemLevel(row.item(4).getTextContent() as Double)
          players.add(player)
        } catch (NullPointerException ignored){}
      }
    }

    players.removeAll { Player player ->
      player.getName() == null || player.getName().trim() == ""
    }

    players.removeAll() { Player player ->
      player.getItemLevel() < filterItemLevel
    }

    return players
  }
}
