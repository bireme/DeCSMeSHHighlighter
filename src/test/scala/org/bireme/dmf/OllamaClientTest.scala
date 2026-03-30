package org.bireme.dmf

import org.scalatest.funsuite.AnyFunSuite

class OllamaClientTest extends AnyFunSuite {
  test("extractResponseContent returns raw text when response is already plain text") {
    assert(OllamaClient.extractResponseContent("Texto traduzido") == "Texto traduzido")
  }

  test("extractResponseContent extracts response field from json payload") {
    val payload = """{"response":"Texto traduzido","done":true}"""
    assert(OllamaClient.extractResponseContent(payload) == "Texto traduzido")
  }

  test("extractResponseContent rejects empty payload") {
    assertThrows[Exception] {
      OllamaClient.extractResponseContent("   ")
    }
  }
}
