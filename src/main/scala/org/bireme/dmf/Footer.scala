package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object Footer {

  def footerXX(): Text.TypedTag[String] =
    div(
      footer(id := "footer")(
        div(cls := "container")(
          div(cls := "row")(
            div(cls := "col-md-8")(
              "DeCS Finder IA é uma ferramenta desenvolvida pela BIREME/OPAS/OMS para apoiar a organização e a recuperação da informação em saúde, por meio da descoberta automática de descritores do tesauro DeCS – Descritores em Ciências da Saúde."
            ),
            div(cls := "col-md-4", id := "footer-logo-bir")(
              img(
                src := "http://logos.bireme.org/img/pt/v_bir_white.svg",
                cls := "img-fluid",
                alt := ""
              )
            )
          ),
          hr()
        )
      ),

      div(id := "powered")(
        div(cls := "container")(
          img(src := "img/powered-pt.svg", alt := "BIREME"),
          br(),
          small("© All rights are reserved'"),
          br(),
          small(
            a(href := "")("Termos e Condições de Uso"),
            " | ",
            a(href := "")("Política de Privacidade")
          )
        )
      )
    )


  def footerX(language: String,
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
      b("DeCS Finder IA"),
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
