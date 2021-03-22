package Scraper.Pages.WoWProgress

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

class PlayerPage extends WoWProgPage {

  static final String BIO_PATH = '//*[@id="primary"]/div/div[2]/div[4]/div[11]'
  static final String RAIDS_PER_WEEK_PATH = '//*[@id="primary"]/div/div[2]/div[4]/div[8]/strong'
  static final String BTAG_PATH = '//*[@id="primary"]/div/div[2]/div[4]/div[3]/span'

  private String pageHTML

  PlayerPage(String playerName, String playerServer) {
    super("/character/us/$playerServer/$playerName")
    pageHTML = getHTML()
  }

  private String getHTML() {
    def request = new URL(pageUrl).openConnection()
    return request.getInputStream().getText(StandardCharsets.UTF_8.name())
  }

  private String getStringFromXpath(String path) {
    TagNode tagNode = new HtmlCleaner().clean(pageHTML)
    Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode)

    XPath xpath = XPathFactory.newInstance().newXPath()
    DTMNodeList output = xpath.evaluate(path, doc, XPathConstants.NODESET) as DTMNodeList

    try {
      return output.item(0).getChildNodes().item(0).getTextContent().trim()
    } catch (NullPointerException ignored) {
      return new String()
    }
  }

  String getBio() {
    return getStringFromXpath(BIO_PATH)
  }

  String getRaidsPerWeek() {
    return getStringFromXpath(RAIDS_PER_WEEK_PATH)
  }

  String getBattleTag() {
    return getStringFromXpath(BTAG_PATH)
  }
}
