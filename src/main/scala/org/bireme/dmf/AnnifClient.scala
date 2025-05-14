package org.bireme.dmf

import sttp.client4.circe.asJson
import sttp.client4.httpurlconnection.HttpURLConnectionBackend
import sttp.client4.{Request, Response, ResponseException, UriContext, basicRequest}
import sttp.model.MediaType
import io.circe.generic.auto._

import scala.collection.mutable

case class AnnifSuggestion(
  label: String,
  notation: Option[String],   // notation can be null
  score: Float,
  uri: String
)
case class AnnifResponse(results: List[AnnifSuggestion])
case class Suggestion(term: String,
                      score: Float)

class AnnifClient(baseAnnifUrl: String) {

  def listProjects(): Either[String, String] = {
    val backend = HttpURLConnectionBackend()
    val request: Request[Either[String, String]] = basicRequest
      .get(uri"$baseAnnifUrl/v1/projects")

    val response: Response[Either[String, String]] = request.send(backend)

    response.body
  }

  def getSuggestions(project_id: String,
                     text: String,
                     limit: Option[Int] = None,
                     threshold: Option[Float] = None,
                     language: Option[String] = None): Either[String, List[AnnifSuggestion]] = {
    val formData: mutable.Map[String, String] = mutable.Map("text" -> text)
    if (limit.isDefined) formData.addOne("limit", limit.get.toString)
    if (threshold.isDefined) formData.addOne("threshold", threshold.get.toString)
    if (language.isDefined) formData.addOne("language", language.get)

    val request1: Request[Either[ResponseException[String], AnnifResponse]] = basicRequest
      .post(uri"$baseAnnifUrl/v1/projects/$project_id/suggest")
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .header("Accept", "application/json")
      .body(formData.toMap)
      .response(asJson[AnnifResponse])

    val response1: Response[Either[ResponseException[String], AnnifResponse]] =
      request1.send(HttpURLConnectionBackend())

    response1.body match {
      case Left(error) => Left(error.toString.replaceAll("\'", "").replaceAll("\"", "\'"))
      case Right(annifResponse) =>
        //Right(annifResponse.results.map(suggestion => Suggestion(term=suggestion.label, score=suggestion.score)))
        Right(annifResponse.results)
    }
  }
}

object AnnifClientApp extends App {
  private val url: String = "http://annif.teste.bireme.br:5000/"
  private val ac: AnnifClient = new AnnifClient(url)

  ac.listProjects() match {
    case Right(projects) => println(projects)
    case Left(error) => println(s"Error: $error")
  }
  ac.getSuggestions(project_id = "omikuji-decs", text = "as mulheres do brasil tem crianças com dengue") match {
    case Right(suggestions) => suggestions.foreach(suggestion => println(s"term=${suggestion.label} score=${suggestion.score}"))
    case Left(error) => println(s"Error: $error")
  }
}
