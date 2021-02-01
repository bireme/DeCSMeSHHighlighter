/*=========================================================================

    DeCSMeSHHighlighter © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHHighlighter/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmf

import java.io.{FileInputStream, InputStream}

import scala.io.{BufferedSource, Source}

/***
  * @author Heitor Barbieri
  * @param i18nIS the i18n file input stream
  */
class I18N(i18nIS: InputStream) {
  val i18nMap: Map[String,(String,String,String,String)] = getI18nContent(Source.fromInputStream(i18nIS, "utf-8"))

  def this(i18nPath: String) = this(new FileInputStream(i18nPath))

  /**
    * Read the input pipe line file with phrases of 4 languages (english, spanish, portuguese nd french) and convert it
    * into a map
    * @param src input internationalization pipe file source. Each line has the following format:
    *                 <key>|<english>|<spanish>|<portuguese>|<.french>
    *                 Empty lines or the ones starting with '#' will be discarted
    * @return map with key containing the english phrase and the value is the tuple (english, spanish, portuguese, french)
    */
  private def getI18nContent(src: BufferedSource): Map[String,(String,String,String,String)] = {
    val outMap = src.getLines().foldLeft(Map[String,(String,String,String,String)]()) {
      case (map, line) =>
        val lineT = line.trim
        if (lineT.isEmpty) map
        else if (lineT.head == '#') map
        else {
          val split = lineT.split(" *\\| *", 5)
          if (split.length == 5) {
            map + (split(0) -> (split(1), split(2), split(3), split(4)))
          } else map
        }
    }
    src.close()
    outMap
  }

  /**
    * Convert a phase from a language (english, spanish, portuguese, french) into another one
    * (english, spanish, portuguese, french)
    * @param key input phrase
    * @param outLang language (english, spanish, portuguese, french) the key should be translated for
    * @return input phase converted into a outLang language
    */
  def translate(key: String,
                outLang: String): String = {
    require(outLang.equalsIgnoreCase("en") || outLang.equalsIgnoreCase("es") ||
      outLang.equalsIgnoreCase("pt") || outLang.equalsIgnoreCase("fr"))

    outLang match {
      case "en" => i18nMap.getOrElse(key, (key,key,key,key))._1
      case "es" => i18nMap.getOrElse(key, (key,key,key,key))._2
      case "pt" => i18nMap.getOrElse(key, (key,key,key,key))._3
      case "fr" => i18nMap.getOrElse(key, (key,key,key,key))._4
      case _ => key
    }
  }
}
