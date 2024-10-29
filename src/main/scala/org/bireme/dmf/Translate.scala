package org.bireme.dmf

import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser
import org.apache.lucene.search.{IndexSearcher, Query, ScoreDoc, TermQuery, TopDocs}
import org.apache.lucene.store.{FSDirectory, MMapDirectory}
import org.bireme.dh.Tools

import java.io.File
import java.nio.file.Path
import scala.util.Try

class Translate(decsPath: String) {
  private val indexPath: Path = new File(decsPath).toPath
  private val directory: FSDirectory = new MMapDirectory(indexPath)
  private val ireader: DirectoryReader = DirectoryReader.open(directory)
  private val isearcher: IndexSearcher = new IndexSearcher(ireader)
  private val parser: StandardQueryParser = new StandardQueryParser()

  def translate(terms: Seq[String],
                outLang: String): Either[String, Seq[String]] = {
    Try {
      if (!outLang.equals("en") && !outLang.equals("es") && !outLang.equals("pt") && !outLang.equals("fr"))
        throw new IllegalArgumentException(s"Invalid language: $outLang")

      terms.foldLeft(Seq[String]()) {
        case (seq, term) =>
          val normalizedTerm: String = Tools.uniformString(term)
          val query: Query = new TermQuery(new Term("term_normalized", normalizedTerm))
          val topDocs: TopDocs = isearcher.search(query, 1)
          val scoreDocs: Array[ScoreDoc] = topDocs.scoreDocs

          val outTerm = scoreDocs.headOption match {
            case Some(sd) =>
              Option(isearcher.storedFields.document(sd.doc).getField("id")) match {
                case Some(id) =>
                  val query2: Query = parser.parse(s"id:${id.stringValue()} AND lang=$outLang AND -synonym:[* TO *]", "term")
                  val topDocs2: TopDocs = isearcher.search(query2, 1)
                  val scoreDocs2: Array[ScoreDoc] = topDocs2.scoreDocs

                  scoreDocs2.headOption match {
                    case Some(sd) =>
                      Option(isearcher.storedFields.document(sd.doc).getField("term")) match {
                        case Some(trm) => trm.stringValue()
                        case None => term
                      }
                    case None => term
                  }
                case None => term
              }
            case None => term
          }
          seq :+ outTerm
      }
    }.toEither match {
      case Right(x) => Right(x)
      case Left(throwable) => Left(throwable.getMessage)
    }
  }

  def close(): Unit = {
    ireader.close()
    directory.close()
  }
}

object Translate extends App {
  val trans: Translate = new Translate("jetty-base/decs/decs")
  val terms = Seq("Arcada Osseodentária", "Barosma crenulatum", "Bibliotecas", "Boca", "Cirurgia Bucal", "Comunicação", "Descritores", "Diagnóstico Precoce",
    "Face", "Fatores de Risco", "Fibra de Lã", "Infecções", "Métodos", "Organização e Administração", "Pesos e Medidas", "Pesquisa", "Prevenção de Doenças",
    "PubMed", "Revisão", "Segurança do Paciente", "Seio Maxilar", "Seleção de Sítio de Tratamento de Resíduos", "Sinais e Sintomas", "Terapêutica", "complicações", "Álcalis")

  Seq("en", "es", "pt", "fr") foreach {
    lang =>
      trans.translate(terms, lang) match {
        case Right(translated) =>
          println(s"\nlang=$lang")
          println(s"terms     =$terms")
          println(s"translated=$translated")
        case Left(error) => System.err.println(s"ERROR: $error")
      }
  }
  trans.close()
}