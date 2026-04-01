package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object ButtonTags {
  private val buttonColor = "#28a745"
  private val buttonBorderColor = "#28a745"
  private val buttonHoverColor = "#218838"
  private val buttonHoverBorderColor = "#1e7e34"

  private def buttonStyle(extra: String = ""): String = {
    val suffix = if (extra.trim.isEmpty) "" else s" ${extra.trim}"
    s"margin-top: 2px; background-color: $buttonColor !important; border-color: $buttonBorderColor !important; color: #fff;$suffix"
  }

  private val buttonHoverOn = s"this.style.backgroundColor='$buttonHoverColor';this.style.borderColor='$buttonHoverBorderColor';"
  private val buttonHoverOff = s"this.style.backgroundColor='$buttonColor';this.style.borderColor='$buttonBorderColor';"

  def importButton(fileInputId: String,
                   language: String,
                   i18n: I18N): Text.TypedTag[String] = {
    val tooltip = i18n.translate(key="Import file", language)

    label(
      cls := "btn btn-success",
      attr("for") := fileInputId,
      style := buttonStyle("cursor: pointer; margin-bottom: 0;"),
      title := tooltip,
      attr("aria-label") := tooltip,
      attr("role") := "button",
      tabindex := 0,
      onclick := s"""window.prepareFileChooser('$fileInputId'); gtag("event", "button_click", { "event_category": "button", "event_label": "Import Button" });""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      attr("onfocusin") := buttonHoverOn,
      attr("onfocusout") := buttonHoverOff
    )(
      span(style := "pointer-events: none;")(i(cls := "fas fa-file-upload fa-fw"))
    )
  }

  def internetButton(language: String,
                     i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := buttonStyle(),
      title := i18n.translate(key="Import", language),
      attr("onclick") :=
        """showDialog(); gtag("event", "button_click", { "event_category": "button", "event_label": "Internet Button" });""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    //)(i(cls := "fa fa-globe"))
    )(i(cls := "fas fa-download fa-fw"))
  }

  def searchButton(originalInputText: String,
                   language: String,
                   i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      id := "sendButton",
      cls  := "btn btn-success",
      style := buttonStyle(),
      title := i18n.translate(key="Send text", language),
      attr("onclick") :=
        s"""document.body.style.cursor = "wait";submitPage(null, "$language", "false"); gtag("event", "button_click", { "event_category": "button", "event_label": "Search Button" });""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    //)(i(cls := "fas fa-search"))
    )(i(cls := "fas fa-check fa-fw"))
  }

  def clearButton(language: String,
                  i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := buttonStyle(),
      title := i18n.translate(key="Clear", language),
      attr("onclick") :=
        """clearTextAreas("""" + language + """"); gtag("event", "button_click", { "event_category": "button", "event_label": "Clear Button"});""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    )(i(cls := "far fa-trash-alt fa-fw"))
  }

  def translateButton(language: String,
                      hasTextToTranslate: Boolean,
                      isLocked: Boolean,
                      i18n: I18N): Text.TypedTag[String] = {
    val tooltip = i18n.translate(key = "Translate", language)
    val isDisabled = !hasTextToTranslate || isLocked

    button(
      attr("type") := "button",
      id := "translateButton",
      cls := "btn btn-success",
      style := buttonStyle(),
      title := tooltip,
      attr("aria-label") := tooltip,
      attr("aria-disabled") := isDisabled.toString,
      (if (isDisabled) attr("disabled") := "disabled" else ()),
      attr("onclick") :=
        """this.disabled = true; this.setAttribute('aria-disabled', 'true'); handleTranslateButtonClick();""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    )(i(cls := "bi bi-translate"))
  }

  def srButton(originalInputText: String,
               language: String,
               i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := buttonStyle("font-size: 15px;"),
      attr("data-bs-toggle") := "button",
      attr("aria-pressed") := "false",
      title := i18n.translate(key="SuperAbstract", language),
      attr("onclick") :=
        s"""document.body.style.cursor = "wait";submitPage(null, "$language", "true"); gtag("event", "button_click", { "event_category": "button", "event_label": "SR Button"});""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    //)(em(i18n.translate(key="SR",language)))
    )(i(cls := "bi bi-arrows-angle-contract"))
  }

  def exportButton(exportTerms: String,
                   language: String,
                   i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := buttonStyle(),
      title := i18n.translate(key="Export to file", language),
      attr("onclick") :=
        """exportTerms("""" + exportTerms + """"); gtag("event", "button_click", { "event_category": "button", "event_label": "Export Button"});""",
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    )(i(cls := "fas fa-file-export fa-fw"))
  }

  def commentsButton(language: String,
                     i18n: I18N): Text.TypedTag[String] = {
    button(
      attr("type") := "button",
      cls  := "btn btn-success",
      style := buttonStyle(),
      title := i18n.translate(key="Send your comments", language),
      attr("onclick") :=
        """window.open("https://contacto.bvsalud.org/chat.php?group=DeCSMeSH%20Finder&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__", "_blank");
          | gtag("event", "button_click", {
          |   "event_category": "button", "event_label": "Comments Button"
          | });""".stripMargin,
      onmouseover := buttonHoverOn,
      onmouseout := buttonHoverOff,
      onfocus := buttonHoverOn,
      onblur := buttonHoverOff
    )(i(cls := "fas fa-comment fa-fw"))
  }
}
