package org.bireme.dmf

import scalatags.Text
import scalatags.Text.all._

object DialogBox {
  def renderDialogBox(): Text.TypedTag[String] = {
    div(
      id := "custom-dialog",
      cls := "dialog-overlay",
      // Conteúdo da caixa
      div(
        cls := "dialog-box",
        h3("Digite a url:"),
        input(`type` := "text", id := "url-input"),
        div(
          cls := "dialog-buttons",
          button(
            id := "open-btn",
            onclick := "handleOpen()",
            "Abrir"
          ),
          button(
            id := "cancel-btn",
            onclick := "handleCancel()",
            "Cancelar"
          )
        )
      )
    )
  }
}
