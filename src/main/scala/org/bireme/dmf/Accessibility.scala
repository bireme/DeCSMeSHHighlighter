package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._
import scalatags.Text.tags2.section

object Accessibility {
  def accessibilityArea(language: String,
                        i18n: I18N): Text.TypedTag[String] = {
    section(id := "barAccessibility")(
      div(cls := "container")(
        div(cls := "row")(
          leftArea(language, i18n),
          rightArea(language, i18n)
        )
      )
    )
  }

  private def leftArea(language: String,
                       i18n: I18N): Text.TypedTag[String] = {
    /* ---------- Coluna 1: links de navegação por acessibilidade ---------- */
    div(cls := "col-md-6", id := "accessibilityTutorial")(
      a(
        href := "#main_container",
        tabindex := "1",
        role := "button"
      )(
        i18n.translate("Main Content", language), " ",
        span(cls := "hiddenMobile")("1")
      ),
      // Links “Menu” e “Busca” permanecem comentados no HTML original.
      a(
        href := "#footer",
        tabindex := "4",
        role := "button"
      )(
        i18n.translate("Footer", language), " ",
        span(cls := "hiddenMobile")("4")
      )
    )
  }

  private def rightArea(language: String,
                        i18n: I18N): Text.TypedTag[String] = {
    /* ---------- Coluna 2: controle de fonte / contraste ---------- */
    div(cls := "col-md-6", id := "accessibilityFontes")(
      a(
        href := "#!",
        id := "fontPlus",
        tabindex := "5",
        attr("aria-hidden") := "true"
      )("+A"),
      a(
        href := "#!",
        id := "fontNormal",
        tabindex := "6",
        attr("aria-hidden") := "true"
      )("A"),
      a(
        href := "#!",
        id := "fontLess",
        tabindex := "7",
        attr("aria-hidden") := "true"
      )("-A"),
      a(
        href := "#!",
        id := "contraste",
        tabindex := "8",
        attr("aria-hidden") := "true"
      )(
        i(cls := "fas fa-adjust"),
        " ",
        i18n.translate("High Contrast", language)
      ),
      a(
        href := s"https://politicas.bireme.org/accesibilidad/${if (language == "fr") "en" else language}",
        role := "button",
        id := "accebilidade",
        tabindex := "9",
        target := "_blank",
        title := i18n.translate("Accessibility", language)
      )(i(cls := "fas fa-wheelchair"))
    )
  }
}
