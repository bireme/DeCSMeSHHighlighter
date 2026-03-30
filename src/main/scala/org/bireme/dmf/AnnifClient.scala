package org.bireme.dmf

import sttp.client4.circe.asJson
import sttp.client4.httpurlconnection.HttpURLConnectionBackend
import sttp.client4.{Request, Response, ResponseException, UriContext, basicRequest}
import sttp.model.MediaType
import io.circe.generic.auto._

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

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
    Try {
      val backend = HttpURLConnectionBackend()
      val request: Request[Either[String, String]] = basicRequest
        .get(uri"$baseAnnifUrl/v1/projects")

      val response: Response[Either[String, String]] = request.send(backend)

      response.body
    } match {
      case Success(value) => value
      case Failure(exception) => Left(exception.getMessage)
    }
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

    //println(s"entrando no getSuggestions - project_id=$project_id text=[$text]")

    Try {
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
          Right(annifResponse.results)
      }
    } match {
      case Success(value) => value
      case Failure(exception) =>
        Left(Option(exception.getMessage).getOrElse(exception.getClass.getSimpleName))
    }
  }
}

object AnnifClientApp {
  def main(args: Array[String]): Unit = {
    val url: String = "http://annif.bvsalud.org:5000/"
    val ac: AnnifClient = new AnnifClient(url)
    val text = "Homens e mulheres\n\ntem dengue\n\n\nno Brasil.\n\n\n\n"

    ac.listProjects() match {
      case Right(projects) => println(projects)
      case Left(error) => println(s"Error: $error")
    }
    ac.getSuggestions(project_id = "ensemble-decs-pt", text = text, limit = Some(15)) match {
      case Right(suggestions) => suggestions.foreach(suggestion => println(s"term=${suggestion.label} score=${suggestion.score}"))
      case Left(error) => println(s"Error: $error")
    }
  }
}
