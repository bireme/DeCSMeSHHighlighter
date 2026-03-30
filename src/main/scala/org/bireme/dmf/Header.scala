package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object Header {
  def headerArea(originalInputText: String,
                 language: String): Text.TypedTag[String] = {
    header(id := "header")(
      div(cls := "container")(
        div(cls := "row", attr("style") := "position: relative;")(
          div(id := "lang")(
            ul(
              languageLink("en", "English", language),
              languageLink("es", "Español", language),
              languageLink("pt", "Português", language),
              languageLink("fr", "Français", language)
            )
          ),
          div(cls := "col-md-4", id := "brand")(
            a(
              href := s"""javascript:submitPageToSite('$language');"""
            )(
              img(
                src := s"decsf/img/logo-${(language)}.svg",
                alt := "",
                cls := "imgBlack"
              )
            )
          ),
          div(cls := "col-md-8")(
          )
        )
      )
    )
  }

  private def languageLink(code: String,
                           label: String,
                           currentLanguage: String): Text.TypedTag[String] = {
    li(
      cls := (if (code == currentLanguage) "current-lang" else "")
    )(
      a(href := "#", onclick := s"clearTextAreas('$code');")(label)
    )
  }

  private def assetLanguage(language: String): String = language match {
    case "en" | "es" | "pt" | "fr" => language
    case _ => "en"
  }
}
