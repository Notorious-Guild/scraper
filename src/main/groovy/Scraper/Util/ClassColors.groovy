package Scraper.Util

class ClassColors {

  static Integer get(String className) {
    switch(className) {
      case "death knight":
        return 12853051
      break
      case "demon hunter":
        return 10694857
      break
      case "druid":
        return 16743690
      break
      case "hunter":
        return 11129457
      break
      case "mage":
        return 4245483
      break
      case "monk":
        return 65430
      break
      case "paladin":
        return 16092346
      break
      case "priest":
        return 16446965
      break
      case "rogue":
        return 16774505
      break
      case "shaman":
        return 28894
      break
      case "warlock":
        return 8882157
      break
      case "warrior":
        return 13081710
      break
      default:
        return 0
      break
    }
  }
}
