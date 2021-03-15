import groovy.util.logging.Slf4j

@Slf4j
class Scraper {
  static void main(String[] args) {
    do {
      ScrapeResource resource = new ScrapeResource()
      Thread.sleep((60 * 5) * 1000)
    } while (true)
  }
}
