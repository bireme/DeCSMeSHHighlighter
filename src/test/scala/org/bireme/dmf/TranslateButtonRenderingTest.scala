package org.bireme.dmf

import org.scalatest.funsuite.AnyFunSuite

class TranslateButtonRenderingTest extends AnyFunSuite {
  private val i18n = new I18N("src/main/webapp/i18n.txt")

  test("translate button is disabled when locked after translation") {
    val html = ButtonTags.translateButton(
      language = "pt",
      hasTextToTranslate = true,
      isLocked = true,
      i18n = i18n
    ).render

    assert(html.contains("""id="translateButton""""))
    assert(html.contains("""aria-disabled="true""""))
    assert(html.contains("""disabled="disabled""""))
  }

  test("translate button disables itself immediately in onclick") {
    val html = ButtonTags.translateButton(
      language = "pt",
      hasTextToTranslate = true,
      isLocked = false,
      i18n = i18n
    ).render

    assert(html.contains("""this.disabled = true;"""))
    assert(html.contains("""this.setAttribute('aria-disabled', 'true');"""))
    assert(html.contains("""handleTranslateButtonClick();"""))
  }

  test("main area keeps translate button disabled on translated response") {
    val html = MainAreas.inputOutputAreas(
      labelx = "Paste your text below",
      inputText = "Texto traduzido",
      originalInputText = "Texto traduzido",
      srText = "",
      annifText = "",
      exportText = "",
      translateButtonLocked = true,
      language = "pt",
      i18n = i18n
    ).render

    assert(html.contains("""id="translateButton""""))
    assert(html.contains("""aria-disabled="true""""))
    assert(html.contains("""disabled="disabled""""))
  }
}
