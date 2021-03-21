package Scraper

import groovy.util.logging.Slf4j

@Slf4j
class Application {
  static void main(String[] args) {
    do {
      new ScrapeResource()
      def frequency = 1000 * (System.getenv("FREQUENCY") as Integer)
      Thread.sleep(frequency)
    } while (true)
  }
}
