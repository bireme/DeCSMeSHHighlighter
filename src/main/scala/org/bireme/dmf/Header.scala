package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object Header {
  def headerArea(originalInputText: String,
                 language: String): Text.TypedTag[String] = {
    header(id := "header")(
      div(cls := "container")(
        div(cls := "row", attr("style") := "position: relative;")(

          /* logotipo */
          div(cls := "col-12")(
            a(
              href := s"""javascript:submitPageToSite('$language');"""
            )(
              img(
                src := s"decsf/img/decs-finder-color-$language.svg",
                alt := "",
                cls := "imgBlack"
              )
            )
          ),

          /* links para mudar o idioma */
          div(id := "language", attr("style") := "z-index: 1")(
            /*a(href := "#", onclick := """submitPage(`""" + originalInputText + """`, "en");""")("English"), " ",
            a(href := "#", onclick := """submitPage(`""" + originalInputText + """`, "es");""")("Español"), " ",
            a(href := "#", onclick := """submitPage(`""" + originalInputText + """`, "pt");""")("Português"), " ",
            a(href := "#", onclick := """submitPage(`""" + originalInputText + """`, "fr");""")("Français")*/
            a(href := "#", onclick := "clearTextAreas('en');")("English"), " ",
            a(href := "#", onclick := "clearTextAreas('es');")("Español"), " ",
            a(href := "#", onclick := "clearTextAreas('pt');")("Português"), " ",
            a(href := "#", onclick := "clearTextAreas('fr');")("Français")
          )
        )
      )
    )
  }
}
