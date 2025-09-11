package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all.{label, _}

object MainAreas {
  def inputOutputAreas(labelx: String,
                       inputText: String,
                       originalInputText: String,
                       srText: String,
                       annifText: String,
                       exportText: String,
                       language: String,
                       i18n: I18N): Text.TypedTag[String] = {
    tag("main")(
      id  := "main_container",
      cls := "padding1"
    )(
      div(cls := "container")(
        div(cls := "row") (
          div(cls := "col-md-8")(
            mainInputArea(labelx, inputText, originalInputText, srText, language, i18n)
          ),
          div(cls := "col-md-4")(
            mainOutputArea(annifText, exportText, language, i18n)
          )
        )
      )
    )
  }

  private def mainInputArea(labelx: String,
                            inputText: String,
                            originalInputText: String,
                            srText: String,
                            language: String,
                            i18n: I18N): Text.TypedTag[String] = {
    val srModifiers: Seq[Modifier] =
      if (srText.nonEmpty) {
        Seq(
          label(
            i18n.translate("SuperAbstract", language) + ":",
            style := "margin-top: 11px;"
          ),
          div(style := "display: flex; align-items: flex-start; max-width: 643px;")(
            srTextArea(srText)
          )
        )
      } else {
        Seq.empty
      }

    div(cls := "form-group col-md-12")(
      Seq[Modifier](
        label(
          i18n.translate(labelx, language) + ":",
          //style := "font-weight: bold;"
        ),
        div(style := "display: flex; align-items: flex-start;")(
          inputTextArea(inputText),
          importButtonGroup(originalInputText, language, i18n)
        )
      ) ++ srModifiers: _*
    )
  }

  private def mainOutputArea(annifText: String,
                             exportText: String,
                             language: String,
                             i18n: I18N): Text.TypedTag[String] = {
    div(cls := "form-group col-md-12")(
      label(i18n.translate("Terms identified by AI", language) + ":" /*, style := "font-weight: bold;"*/),
      div(style := "display: flex; align-items: flex-start;")(
        annifArea(annifText),
        exportButtonGroup(exportText, language, i18n)
      )
    )
  }

  private def inputTextArea(inpText: String): Text.TypedTag[String] = {
    div(
      id := "textWithTooltips",
      cls := "p-3 border rounded",
      style := "flex-grow: 1; resize: vertical; overflow: auto;",
      attr("spellcheck")      := "false",
      attr("contenteditable") := (if (inpText.trim.isEmpty) "true" else "false")
    )(raw(inpText))
  }

  private def srTextArea(srText: String): Text.TypedTag[String] = {
    div(
      id := "superResumos",
      cls := "p-3 border rounded",
      style := "flex-grow: 1; height: 8em; overflow: auto;",
      attr("spellcheck")      := "false",
      attr("contenteditable") := "false"
    )(raw(srText))
  }

  private def annifArea(annifText: String): Text.TypedTag[String] = {
    div(
      id := "textWithTooltipsAnnif",
      cls := "p-3 border rounded",
      style := "flex-grow: 1; resize: vertical; overflow-x: auto; white-space: pre;",
      attr("spellcheck")      := "false",
      attr("contenteditable") := "false"
    )(raw(annifText))
  }

  private def importButtonGroup(originalInputText: String,
                                language: String,
                                i18n: I18N): Text.TypedTag[String] = {
    val fileInputId = "fileChooser"

    div(
    /*  cls := "btn-group",
      attr("role")       := "group",
      attr("aria-label") := "Import Button Group", */
      style := "display: flex; flex-direction: column; justify-content: flex-start; margin-left: 10px;"
    )(
      input(
        id := fileInputId,
        `type` := "file",
        hidden := "hidden",
        attr("accept") := ".txt,.pdf",
        // Usando onchange do Scalatags e referenciando a função global
        onchange := "window.handleFChange(event);"
      ),
      ButtonTags.importButton(language, i18n),
      //ButtonTags.internetButton(language, i18n),
      //ButtonTags.searchButton(originalInputText, language, i18n),
      ButtonTags.clearButton(language, i18n),
      //ButtonTags.srButton(originalInputText, language, i18n),
      DialogBox.renderDialogBox()
    )
  }

  private def exportButtonGroup(exportText: String,
                                language: String,
                                i18n: I18N): Text.TypedTag[String] = {
    div(
      /* cls := "btn-group",
      attr("role")       := "group",
      attr("aria-label") := "Export Button Group", */
      style := "display: flex; flex-direction: column; justify-content: flex-start; margin-left: 10px;"
    )(
      ButtonTags.exportButton(exportText, language, i18n),
      ButtonTags.commentsButton(language, i18n),
    )
  }
}
