package org.bireme.dmf

import org.scalatest.funsuite.AnyFunSuite

class InputProcessingTest extends AnyFunSuite {
  test("resolveInputLanguage keeps explicit supported language") {
    assert(InputProcessing.resolveInputLanguage("fr", "pt") == "fr")
  }

  test("resolveInputLanguage uses detected language when auto detect is selected") {
    assert(InputProcessing.resolveInputLanguage(InputProcessing.AutoDetectLanguage, "es") == "es")
  }

  test("prepareInputForProcessing translates text to interface language when requested") {
    val result = InputProcessing.prepareInputForProcessing(
      inputText = "Bonjour le monde",
      currentInputLang = "fr",
      interfaceLanguage = "pt",
      translateRequested = true,
      translateSourceLang = Some("fr"),
      translateFn = (text, sourceLang, targetLang) => {
        assert(text == "Bonjour le monde")
        assert(sourceLang == "fr")
        assert(targetLang == "pt")
        Right("Ola mundo")
      }
    )

    assert(result == Right(InputProcessing.PreparedInput("pt", "Ola mundo", "Ola mundo")))
  }

  test("prepareInputForProcessing keeps original text when source and target languages are equal") {
    val result = InputProcessing.prepareInputForProcessing(
      inputText = "Texto original",
      currentInputLang = "pt",
      interfaceLanguage = "pt",
      translateRequested = true,
      translateSourceLang = Some("pt"),
      translateFn = (_, _, _) => fail("translateFn should not be called when source and target languages are equal")
    )

    assert(result == Right(InputProcessing.PreparedInput("pt", "Texto original", "Texto original")))
  }

  test("prepareInputForProcessing returns translation error without processing further") {
    val result = InputProcessing.prepareInputForProcessing(
      inputText = "Bonjour",
      currentInputLang = "fr",
      interfaceLanguage = "pt",
      translateRequested = true,
      translateSourceLang = Some("fr"),
      translateFn = (_, _, _) => Left("translation error")
    )

    assert(result == Left("translation error"))
  }
}
