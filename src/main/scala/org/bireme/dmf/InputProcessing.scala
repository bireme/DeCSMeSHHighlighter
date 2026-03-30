package org.bireme.dmf

object InputProcessing {
  val AutoDetectLanguage: String = "All languages"

  private val supportedLanguages: Set[String] = Set("en", "es", "pt", "fr")

  final case class PreparedInput(inputLang: String,
                                 inputText: String,
                                 originalInputText: String)

  def normalizeSelectedLanguage(value: String): String = Option(value).map(_.trim).getOrElse("") match {
    case "" => AutoDetectLanguage
    case AutoDetectLanguage => AutoDetectLanguage
    case lang if supportedLanguages.contains(lang) => lang
    case _ => "en"
  }

  def resolveInputLanguage(selectedLanguage: String,
                           detectedLanguage: => String): String = normalizeSelectedLanguage(selectedLanguage) match {
    case AutoDetectLanguage =>
      val detected = Option(detectedLanguage).map(_.trim).getOrElse("")
      if (supportedLanguages.contains(detected)) detected else "en"
    case lang => lang
  }

  def prepareInputForProcessing(inputText: String,
                                currentInputLang: String,
                                interfaceLanguage: String,
                                translateRequested: Boolean,
                                translateSourceLang: Option[String],
                                translateFn: (String, String, String) => Either[String, String]): Either[String, PreparedInput] = {
    val resolvedInterfaceLanguage =
      if (supportedLanguages.contains(interfaceLanguage)) interfaceLanguage else currentInputLang

    if (!translateRequested || inputText.trim.isEmpty) {
      Right(PreparedInput(currentInputLang, inputText, inputText))
    } else {
      val sourceLanguage = translateSourceLang.map(normalizeSelectedLanguage) match {
        case Some(AutoDetectLanguage) | None => currentInputLang
        case Some(lang) => lang
      }

      if (sourceLanguage == resolvedInterfaceLanguage) {
        Right(PreparedInput(resolvedInterfaceLanguage, inputText, inputText))
      } else {
        translateFn(inputText, sourceLanguage, resolvedInterfaceLanguage).map { translatedText =>
          PreparedInput(
            inputLang = resolvedInterfaceLanguage,
            inputText = translatedText,
            originalInputText = translatedText
          )
        }
      }
    }
  }
}
