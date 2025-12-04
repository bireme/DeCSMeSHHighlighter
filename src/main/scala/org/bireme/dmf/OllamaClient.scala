package org.bireme.dmf

import io.github.ollama4j.Ollama
import io.github.ollama4j.models.generate.OllamaGenerateRequest
import io.github.ollama4j.models.response.OllamaResult

import play.api.libs.json._

import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}
import scala.jdk.CollectionConverters._

class OllamaClient(ollamaHost: String,
                   ollamaPort: Option[Int]) {
  private val url: String = s"http://$ollamaHost:${ollamaPort.getOrElse(11434)}"

  private val ollamaAPI: Ollama = new Ollama(url)
  if (!ollamaAPI.ping())  throw new Exception(s"It is not possible to connect with Ollama server at: $url")

  def getModelNames(): Try[Set[String]] = Try(ollamaAPI.listModels().asScala.map(model => model.getModelName).toSet)

  def chat(input: String,
           model: String): Try[String] = {
    Try {
      val result: OllamaResult =
        ollamaAPI.generate(OllamaGenerateRequest.builder()
            .withModel(model)
            .withPrompt(input)
            .build(),
          null)
      val chatResult: String = result.getResponse()
      val parsed: JsValue = Json.parse(chatResult)
      val result2: Option[String] = (parsed \ "response").asOpt[String]

      result2.getOrElse(throw new Exception("Super Abstract generation error."))
    }
  }
}

object OllamaClient {
  private def usage(): Unit = {
    System.err.println("usage: OllamaClient <options>")
    System.err.println("options:")
    System.err.println("\t(--listModels|-chat=<model>)  'listModels' will list the model's name and 'chat' will start a chat with the model <model> (enter 'exit' to exit the conversation)")
    System.err.println("\t[-ollamaServer=<str>]         host name. Default value is localhost")
    System.err.println("\t[-ollamaPort=<num>]           port the Ollama is listening. Default value is 11434")
    System.exit(-1)
  }

  def main(args: Array[String]): Unit = {
    val params: Map[String, String] = args.foldLeft[Map[String, String]](Map()) {
      case (map, par) =>
        val split: Array[String] = par.split(" *= *", 2)
        val split0: String = split.head

        split.length match {
          case 1 if split0.length > 2 => map + (split0.substring(2) -> "")
          case 2 if split0.length > 1 => map + (split0.substring(1) -> split(1))
          case _ =>
            usage()
            map
        }
    }

    println(params.keys.head)
    if (!params.contains("listModels") && !params.contains("chat")) usage()

    val ollamaServer: String = params.getOrElse("ollamaServer", "localhost")
    val ollamaPort: Option[Int] = params.get("ollamaPort").map(_.toInt)
    val ollamaClient: OllamaClient = new OllamaClient(ollamaServer, ollamaPort)

    if (params.contains("listModels")) showModels(ollamaClient)
    else {
      val model: String = params("chat")
      chat(ollamaClient, model)
    }
  }

  private def showModels(ollamaClient: OllamaClient): Unit = {
    ollamaClient.getModelNames() match {
      case Success(names) =>
        println("Model names:")
        names.foreach(name => println(s"\t$name"))
      case Failure(exception) =>
        System.err.println(s"Model names error: ${exception.toString}")
    }
  }

  private def chat(ollamaClient: OllamaClient,
                   modelName: String): Unit = {
    var hasNext = true

    while (hasNext) {
      val abs: String = readLine("Abstract: ")

      if (abs.trim.toLowerCase == "exit") hasNext = false
      else ollamaClient.chat(abs, modelName) match {
        case Success(sa) => println(s"Super Abstract: $sa")
        case Failure(_) => println(s"Super Abstract generation error.")
      }
    }

    println("It's my pleasure to serve you. Bye.")
  }
}