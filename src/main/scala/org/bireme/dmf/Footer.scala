package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object Footer {
  def footer(language: String,
             i18n: I18N): Text.TypedTag[String] = {
    tag("footer") (
      id  := "footer",
      cls := "padding1"
    )(
      div(cls := "container")(
        div(cls := "row")(
          leftFooter(language, i18n),
          rightFooter(language)
        )
      )
    )
  }

  private def leftFooter(language: String,
                         i18n: I18N): Text.TypedTag[String] = {
    div(cls := "col-md-5")(
      b("DeCS Finder"),
      br(),

      /* Termos de uso */
      a(
        href :=
          s"http://politicas.bireme.org/terminos/${ if (language == "fr") "en" else language }",
        target := "_blank"
      )(i18n.translate("Terms and conditions of use", language)),

      " ",  // espaço entre os links (pode usar "&nbsp;" ou CSS)

      /* Política de privacidade */
      a(
        href :=
          s"http://politicas.bireme.org/privacidad/${ if (language == "fr") "en" else language }",
        target := "_blank"
      )(i18n.translate("Privacy policy", language))
    )
  }

  private def rightFooter(language: String): Text.TypedTag[String] = {
    div(cls := "col-md-7 text-right")(
      {
        val homeUrl: String = language match {
          case "es" => "https://www.bireme.org/es/home-espanol/"
          case "pt" => "https://www.bireme.org/"
          case _    => "https://www.bireme.org/en/home-english/"
        }

        a(href := homeUrl, target := "_blank")(
          img(
            src  := s"http://logos.bireme.org/img/$language/h_bir_white.svg",
            alt  := "",
            cls  := "img-fluid"
          )
        )
      }
    )
  }
}
