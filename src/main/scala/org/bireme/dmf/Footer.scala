package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object Footer {

  def footerX(language: String,
              i18n: I18N): Text.TypedTag[String] = {
    val lang: String = if language.equals("fr") then "en" else footerAssetLanguage(language)

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
          small(s"© ${i18n.translate("All rights are reserved", language)}"),
          br(),
          small(
            a(
              href := s"https://politicas.bireme.org/terminos/$lang",
              target := "_blank"
            )(i18n.translate("Terms and conditions of use", language)),
            " | ",
            a(
              href := s"https://politicas.bireme.org/privacidad/$lang",
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
    val url = language match {
      case "pt" => "https://www.bireme.org/"
      case "es" => "https://www.bireme.org/es/home-espanol/"
      case _ => "https://www.bireme.org/en/home-english/"
    }
    
    div(cls := "col-md-4", id := "footer-logo-bir")(
      a(
        href := url,
        target := "_blank",
        rel := "noopener noreferrer"
      )(
        img(
          src := s"https://logos.bireme.org/img/${footerAssetLanguage(language)}/v_bir_white.svg",
          cls := "img-fluid",
          alt := "BIREME"
        )
      )
    )
  }

  private def footerDescription(language: String): String = language match {
    case "es" =>
      "DeCS Finder IA es una herramienta desarrollada por BIREME/OPS/OMS para apoyar la organización y la recuperación de la información en salud, por medio del descubrimiento automático de descriptores del tesauro DeCS - Descriptores en Ciencias de la Salud."
    case "fr" =>
      "DeCS Finder IA est un outil développé par la BIREME/OPS/OMS pour soutenir l’organisation et la récupération de l’information en santé, grâce à la découverte automatique des descripteurs du thésaurus DeCS – Descripteurs en Sciences de la Santé."
    case "pt" =>
      "DeCS Finder IA é uma ferramenta desenvolvida pela BIREME/OPAS/OMS para apoiar a organização e a recuperação da informação em saúde, por meio da descoberta automática de descritores do tesauro DeCS – Descritores em Ciências da Saúde."
    case _ =>
      "DeCS Finder IA is a tool developed by BIREME/PAHO/WHO to support the organization and retrieval of health information through the automatic discovery of descriptors from the DeCS - Health Sciences Descriptors thesaurus."
  }

  private def footerAssetLanguage(language: String): String = language match {
    case "es" => "es"
    case "pt" => "pt"
    case "fr" => "fr"
    case _ => "en"
  }
}
