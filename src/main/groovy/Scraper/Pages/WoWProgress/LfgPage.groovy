package Scraper.Pages.WoWProgress

import Scraper.Models.Player
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList
import org.htmlcleaner.CleanerProperties
import org.htmlcleaner.DomSerializer
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import org.w3c.dom.Document

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.nio.charset.StandardCharsets

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

    for (def i = 0; i < table.getLength(); i++) {
      players.add(new Player())
    }

    for (def x = 1; x < table.getLength(); x++) {

      def rowData = table.item(x).getChildNodes()

      def serverData = rowData.item(3).getChildNodes().item(0).getChildNodes().item(0).getChildNodes()
      def nameData = rowData.item(0).getChildNodes().item(0).getChildNodes()
      def ilvlData = rowData.item(4).getChildNodes()

      // add names
      for (def i = 0; i < nameData.getLength(); i++) {
        def preName = nameData.item(i).toString()
        def finalName = preName.split("\\[#text: ")[1].replaceAll("]", new String())
        players.get(x).setName(finalName)
      }

      // add servers
      for (def i = 0; i < serverData.getLength(); i++) {
        def preName = serverData.item(i).toString()
        def finalName = preName.split("\\[#text: ")[1].replaceAll("]", new String())
        players.get(x).setServer(finalName)
      }

      // add ilvl
      for (def i = 0; i < ilvlData.getLength(); i++) {
        def preName = ilvlData.item(i).toString()
        def finalName = preName.split("\\[#text: ")[1].replaceAll("]", new String())
        players.get(x).setItemLevel(finalName as Double)
      }
    }

    players.removeAll { Player player ->
      player.getName() == null
    }

    players.removeAll() { Player player ->
      player.getItemLevel() < filterItemLevel
    }

    return players
  }
}
