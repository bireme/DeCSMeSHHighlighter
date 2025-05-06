package org.bireme.dmf

import org.apache.lucene.document.Document
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.search.{IndexSearcher, TermQuery, TopDocs}
import org.apache.lucene.store.{FSDirectory, MMapDirectory}

import java.io.File
import java.nio.file.Path
import scala.util.Try

class MarkPrefSuffix(decsPath: String) {
  private val indexPath: Path = new File(decsPath).toPath
  private val directory: FSDirectory = new MMapDirectory(indexPath)
  private val ireader: DirectoryReader = DirectoryReader.open(directory)
  private val isearcher: IndexSearcher = new IndexSearcher(ireader)

  def close(): Unit = {
    Try {
      ireader.close()
      directory.close()
    }
  }

  def prefSuffix(term: String,
                 uniqueId: String,
                 termLang: String,
                 tipLang:  String): String = {
    val topDocs: TopDocs = isearcher.search(new TermQuery(new Term("unique_id", uniqueId.toUpperCase)), 1)
    topDocs.scoreDocs.headOption match {
      case Some(sd) =>
        val doc: Document = isearcher.storedFields.document(sd.doc)
        val descriptorField: String = tipLang.trim.toLowerCase() match {
          case "en" => "descriptor_en"
          case "es" => "descriptor_es"
          case "pt" => "descriptor_pt"
          case "fr" => "descriptor_fr"
          case _ => "descriptor_en"
        }
        val scopeNoteField: String = tipLang.trim.toLowerCase() match {
          case "en" => "scopeNote_en"
          case "es" => "scopeNote_es"
          case "pt" => "scopeNote_pt"
          case _ => "scopeNote_en"
        }
        val descriptor: String = Option(doc.get(descriptorField)).getOrElse("")
        val scopeNote: String = Option(doc.get(scopeNoteField)).getOrElse("").replace('"', '\'')
        val decsId: String = Option(doc.get("decs_id")).getOrElse("")
        val treeNumber: Option[Array[String]] = Option(doc.getValues("tree_number"))

        mark(term, descriptor, scopeNote, decsId, treeNumber, tipLang)
      case None => term
    }
  }

  def prefSuffix1(term: String,
                  termLang: String,
                  tipLang:  String): String = {
    val inDescriptorField: String = termLang.trim.toLowerCase() match {
      case "en" => "descriptor_en"
      case "es" => "descriptor_es"
      case "pt" => "descriptor_pt"
      case "fr" => "descriptor_fr"
      case _ => "descriptor_en"
    }
    val outDescriptorField: String = tipLang.trim.toLowerCase() match {
      case "en" => "descriptor_en"
      case "es" => "descriptor_es"
      case "pt" => "descriptor_pt"
      case "fr" => "descriptor_fr"
      case _ => "descriptor_en"
    }
    val topDocs: TopDocs = isearcher.search(new TermQuery(new Term(inDescriptorField, term)), 1)
    topDocs.scoreDocs.headOption match {
      case Some(sd) =>
        val doc: Document = isearcher.storedFields.document(sd.doc)
        val scopeNoteField: String = tipLang.trim.toLowerCase() match {
          case "en" => "scopeNote_en"
          case "es" => "scopeNote_es"
          case "pt" => "scopeNote_pt"
          case _ => "scopeNote_en"
        }
        val descriptor: String = Option(doc.get(outDescriptorField)).getOrElse("")
        val scopeNote: String = Option(doc.get(scopeNoteField)).getOrElse("").replace('"', '\'')
        val treeNumber: Option[Array[String]] = Option(doc.getValues("tree_number"))
        val decsId: String = Option(doc.get("decs_id")).getOrElse("")

        mark(term, descriptor, scopeNote, decsId, treeNumber, tipLang)
      case None =>
        println(s"Nao achou o termo:[$term] termLang:$termLang tipLang:$tipLang")
        term
    }
  }

  private def mark(term: String,
                   descriptor: String,
                   scopeNote: String,
                   decsId: String,
                   treeNumber: Option[Array[String]],
                   language: String): String = {
    val treeNumberStr: String = treeNumber.map(arr => s"<br/><br/>${arr.mkString("<br/>")}").getOrElse("")
    val content: String = s"<b>[$descriptor]</b><br/><br/>$scopeNote$treeNumberStr"
    val lang: String = language match {
      case "pt" => ""
      case _ => s"/$language"
    }

    val ret = s"""<a href="https://decs.bvsalud.org$lang/ths/resource/?id=$decsId&q=$descriptor&filter=ths_exact_term"
       class="tooltip-link"
       data-bs-toggle="tooltip"
       data-bs-html="true"
       title="$content"
       target="_blank">$term</a>"""

    //println(s"term=$term ret=$ret")
    ret
  }
}
