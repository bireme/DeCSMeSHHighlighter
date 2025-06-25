package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._
import scalatags.Text.tags2.section

object LanguageTags {
  def languageArea(inputLang: String,
                   outputLang: String,
                   termTypes: Seq[String],
                   language: String,
                   i18n: I18N): Text.TypedTag[String] = {
    section(id := "filter")(
      div(cls := "container")(
        div(cls := "row")(
          textLangArea(inputLang, language, i18n),
          textTermArea(outputLang, language, i18n),
          typeOfTermArea(termTypes, language, i18n)
        )
      )
    )
  }

  private def textLangArea(inputLang: String,
                           language: String,
                           i18n: I18N): Text.TypedTag[String] = {
    div(cls := "form-group col-md-4")(
      label(`for` := "inputTextLanguage")(
        s"${i18n.translate("Language of your text", language)}:"
      ),
      select(
        name := "",                                 // -- deixe vazio como no HTML original
        id   := "inputTextLanguage",
        cls  := "form-control"
      )(
        option(
          value := "All languages",
          if (inputLang == "All languages") selected := "" else ()
        )(i18n.translate("I don't know", language)),

        option(
          value := "en",
          if (inputLang == "en") selected := "" else ()
        )(i18n.translate("English", language)),

        option(
          value := "es",
          if (inputLang == "es") selected := "" else ()
        )(i18n.translate("Spanish", language)),

        option(
          value := "pt",
          if (inputLang == "pt") selected := "" else ()
        )(i18n.translate("Portuguese", language)),

        option(
          value := "fr",
          if (inputLang == "fr") selected := "" else ()
        )(i18n.translate("French", language))
      )
    )
  }

  private def textTermArea(outputLang: String,
                           language: String,
                           i18n: I18N): Text.TypedTag[String] = {
    div(cls := "form-group col-md-4")(
      label(`for` := "outputTextLanguage")(
        s"${i18n.translate("Language of the terms", language)}:"
      ),
      select(
        name := "",
        id   := "outputTextLanguage",
        cls  := "form-control"
      )(
        option(
          value := "en",
          if (outputLang == "en") selected := "" else ()
        )(i18n.translate("English", language)),

        option(
          value := "es",
          if (outputLang == "es") selected := "" else ()
        )(i18n.translate("Spanish", language)),

        option(
          value := "pt",
          if (outputLang == "pt") selected := "" else ()
        )(i18n.translate("Portuguese", language)),

        option(
          value := "fr",
          if (outputLang == "fr") selected := "" else ()
        )(i18n.translate("French", language))
      )
    )
  }

  private def typeOfTermArea(termTypes: Seq[String],
                             language: String,
                             i18n: I18N): Text.TypedTag[String] = {
    div(cls := "form-group col-md-4")(
      /* rótulo */
      label(`for` := "")(
        s"${i18n.translate("Types of terms", language)}:"
      ),

      /* lista múltipla */
      select(
        multiple := "multiple",
        cls  := "selectpicker form-control",
        id   := "termTypes",
        placeholder := ""
      )(
        /* “Descritores” */
        option(
          value := "Descriptors",
          if (termTypes.contains("Descriptors")) selected := "" else ()
        )(i18n.translate("Descriptors", language)),

        /* “Qualificadores” */
        option(
          value := "Qualifiers",
          if (termTypes.contains("Qualifiers")) selected := "" else ()
        )(i18n.translate("Qualifiers", language))
      )
    )
  }
}
