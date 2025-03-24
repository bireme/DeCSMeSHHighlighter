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
                 language: String): String = {
    val topDocs: TopDocs = isearcher.search(new TermQuery(new Term("unique_id", uniqueId.toUpperCase)), 1)
    topDocs.scoreDocs.headOption match {
      case Some(sd) =>
        val doc: Document = isearcher.storedFields.document(sd.doc)
        val descriptorField: String = language.trim.toLowerCase() match {
          case "en" => "descriptor_en"
          case "es" => "descriptor_es"
          case "pt" => "descriptor_pt"
          case "fr" => "descriptor_fr"
          case _ => "descriptor_en"
        }
        val scopeNoteField: String = language.trim.toLowerCase() match {
          case "en" => "scopeNote_en"
          case "es" => "scopeNote_es"
          case "pt" => "scopeNote_pt"
          case _ => "scopeNote_en"
        }
        val descriptor: String = Option(doc.get(descriptorField)).getOrElse("")
        val scopeNote: String = Option(doc.get(scopeNoteField)).getOrElse("")
        val decsId: String = Option(doc.get("decs_id")).getOrElse("")

        mark(term, descriptor, scopeNote, decsId)
      case None => term
    }
  }

  private def mark(term: String,
                   descriptor: String,
                   scopeNote: String,
                   decsId: String): String = {
    val content: String = s"<b>[$descriptor]</b><br/>$scopeNote"
    //val content1: String = if (content.size > 90) content.substring(0, 90) + "..." else content

    //s"""<a href="https://decs.bvsalud.org/ths/resource/?id=$decsId" class="text-primary" data-toggle="tooltip" data-bs-html="true" title="$content1" target="_blank">$term</a>"""
    s"""<a href="https://decs.bvsalud.org/ths/resource/?id=$decsId"
       class="tooltip-link"
       data-bs-toggle="tooltip"
       data-bs-html="true"
       title="$content"
       target="_blank">
      $term
    </a>"""
  }
}
