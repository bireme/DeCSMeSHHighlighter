package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object ButtonTags {

  def importButton(language: String, i18n: I18N): Text.TypedTag[String] = {
    val fileInputId = "fileChooser"

    button(
      attr("type") := "button",
      cls := "btn btn-success",
      style := "margin-top:2px;",
      title := i18n.translate(key="Import file", language),
      onclick := s"""document.body.style.cursor = "wait"; document.getElementById('$fileInputId').click(); gtag("event", "button_click", { "event_category": "button", "event_label": "Import Button" });"""
    //)(i(cls := "fas fa-archive"))
    //)(i(cls := "fas fa-file-download"))
    )(i(cls := "fas fa-file-upload"))
  }

  def internetButton(language: String,
                     i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := "margin-top: 2px;",
      title := i18n.translate(key="Import", language),
      attr("onclick") :=
        """showDialog(); gtag("event", "button_click", { "event_category": "button", "event_label": "Internet Button" });"""
    //)(i(cls := "fa fa-globe"))
    )(i(cls := "fas fa-download"))
  }

  def searchButton(originalInputText: String,
                   language: String,
                   i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := "margin-top: 2px;",
      title := i18n.translate(key="Send text", language),
      attr("onclick") :=
        """document.body.style.cursor = "wait";submitPage(`""" + originalInputText + """`, """" + language + """", "false"); gtag("event", "button_click", { "event_category": "button", "event_label": "Search Button" });"""
    //)(i(cls := "fas fa-search"))
    )(i(cls := "fas fa-check"))
  }

  def clearButton(language: String,
                  i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := "margin-top: 2px;",
      title := i18n.translate(key="Clear", language),
      attr("onclick") :=
        """clearTextAreas("""" + language + """"); gtag("event", "button_click", { "event_category": "button", "event_label": "Clear Button"});"""
    )(i(cls := "far fa-trash-alt"))
  }

  def srButton(originalInputText: String,
               language: String,
               i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := "margin-top: 2px; font-size: 15px;",
      attr("data-bs-toggle") := "button",
      attr("aria-pressed") := "false",
      title := i18n.translate(key="SuperAbstract", language),
      attr("onclick") :=
        """document.body.style.cursor = "wait";submitPage(`""".stripMargin + originalInputText + """`, """" + language + """", "true"); gtag("event", "button_click", { "event_category": "button", "event_label": "SR Button"});"""
    //)(em(i18n.translate(key="SR",language)))
    )(i(cls := "bi bi-arrows-angle-contract"))
  }

  def exportButton(exportTerms: String,
                   language: String,
                   i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := "margin-top: 2px;",
      title := i18n.translate(key="Export to file", language),
      attr("onclick") :=
        """exportTerms("""" + exportTerms + """"); gtag("event", "button_click", { "event_category": "button", "event_label": "Export Button"});"""
    )(i(cls := "fas fa-file-export"))
  }

  def commentsButton(language: String,
                     i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := "margin-top: 2px;",
      title := i18n.translate(key="Send your comments", language),
      attr("onclick") :=
        """window.open("https://contacto.bvsalud.org/chat.php?group=DeCSMeSH%20Finder&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__", "_blank");
          | gtag("event", "button_click", {
          |   "event_category": "button", "event_label": "Comments Button"
          | });""".stripMargin
    )(i(cls := "fas fa-comment"))
  }
}
