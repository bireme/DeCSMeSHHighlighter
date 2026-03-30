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
    div(
      footer(id := "footer")(
        div(cls := "container")(
          div(cls := "row")(
            leftFooter(language),
            rightFooter(language)
          ),
          hr()
        )
      ),
      div(id := "powered")(
        div(cls := "container")(
          img(
            src := s"decsf/img/powered-${footerAssetLanguage(language)}.svg",
            alt := "BIREME"
          ),
          br(),
          small("© All rights are reserved'"),
          br(),
          small(
            a(
              href := s"https://politicas.bireme.org/terminos/${footerAssetLanguage(language)}",
              target := "_blank"
            )(i18n.translate("Terms and conditions of use", language)),
            " | ",
            a(
              href := s"https://politicas.bireme.org/privacidad/${footerAssetLanguage(language)}",
              target := "_blank"
            )(i18n.translate("Privacy policy", language))
          )
        )
      )
    )
  }

  private def leftFooter(language: String): Text.TypedTag[String] = {
    div(cls := "col-md-8")(
      footerDescription(language)
    )
  }

  private def rightFooter(language: String): Text.TypedTag[String] = {
    div(cls := "col-md-4", id := "footer-logo-bir")(
      img(
        src := s"https://logos.bireme.org/img/${footerAssetLanguage(language)}/v_bir_white.svg",
        cls := "img-fluid",
        alt := ""
      )
    )
  }

  private def footerDescription(language: String): String = language match {
    case "es" =>
      "DeCS Finder IA es una herramienta desarrollada por BIREME/OPS/OMS para apoyar la organización y la recuperación de la información en salud, por medio del descubrimiento automático de descriptores del tesauro DeCS - Descriptores en Ciencias de la Salud."
    case "en" | "fr" =>
      "DeCS Finder IA is a tool developed by BIREME/PAHO/WHO to support the organization and retrieval of health information through the automatic discovery of descriptors from the DeCS - Health Sciences Descriptors thesaurus."
    case _ =>
      "DeCS Finder IA é uma ferramenta desenvolvida pela BIREME/OPAS/OMS para apoiar a organização e a recuperação da informação em saúde, por meio da descoberta automática de descritores do tesauro DeCS – Descritores em Ciências da Saúde."
  }

  private def footerAssetLanguage(language: String): String = language match {
    case "es" => "es"
    case "pt" => "pt"
    case _ => "en"
  }
}
