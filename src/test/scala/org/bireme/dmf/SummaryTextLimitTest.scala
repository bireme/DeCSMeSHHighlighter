package org.bireme.dmf

import org.scalatest.funsuite.AnyFunSuite

class SummaryTextLimitTest extends AnyFunSuite {
  test("summary input keeps text with at most one thousand words") {
    val input = (1 to 1000).map(index => s"word$index").mkString(" ")

    assert(DMFServlet.limitTextByWords(input, 1000) == input)
  }

  test("summary input is truncated after the one thousandth word") {
    val input = (1 to 1005).map(index => s"word$index").mkString(" ")
    val result = DMFServlet.limitTextByWords(input, 1000)

    assert(result.endsWith("word1000 ..."))
    assert(!result.contains("word1001"))
    assert("\\S+".r.findAllIn(result.stripSuffix(" ...")).length == 1000)
  }
}
