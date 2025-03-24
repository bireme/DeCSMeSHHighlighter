package org.bireme.dmf

import bruma.iterator.IsisRecordIterator
import bruma.master.Record

import java.io.File
import java.nio.file.Path
import org.apache.lucene.analysis.core.KeywordAnalyzer
import org.apache.lucene.document.{Document, Field, StoredField, StringField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.{Directory, FSDirectory}

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object ImportDeCS {
  private def usage(): Unit = {
    Console.err.println("Convert an Isis DeCS database into a Lucene index")
    Console.err.println("usage: ImportDecs <options>")
    Console.err.println("\toptions:")
    Console.err.println("\t-master=<path> - DeCS master file path")
    Console.err.println("\t-lucene=<path> - Lucene index path")
    Console.err.println("\t[-encoding=<str>] - master file character encoding. Default is ISO8859-1")
    System.exit(1)
  }
  

  def main(args: Array[String]): Unit = {
    val parameters: Map[String, String] = args.foldLeft[Map[String, String]](Map()) {
      case (map, par) =>
        val split = par.split(" *= *", 2)
        if (split.length == 2) map + ((split(0).substring(1), split(1)))
        else {
          usage()
          map
        }
    }

    if (!Set("master", "lucene").forall(parameters.contains)) usage()

    impDeCS(masterPath=parameters("master"), lucenePath=parameters("lucene"), inputEncoding=parameters.get("encoding")) match {
      case Success(_) =>
        println("Import finished succesfully")
        System.exit(0)
      case Failure(exception) =>
        Console.err.println(s"Import error: ${exception.getMessage}")
        System.exit(1)
    }
  }

  def impDeCS(masterPath: String,
              lucenePath: String,
              inputEncoding: Option[String]): Try[Unit] = {
    Try {
      val recIterator: Iterator[Record] = new IsisRecordIterator(masterPath, inputEncoding.getOrElse("ISO8859-1")).iterator().asScala
      val indexPath: Path = new File(lucenePath).toPath
      val directory: Directory = FSDirectory.open(indexPath)
      val config: IndexWriterConfig = new IndexWriterConfig(new KeywordAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE)
      val iwriter: IndexWriter = new IndexWriter(directory, config)

      recIterator.foreach{
        rec =>
          if (rec.getStatus == Record.Status.ACTIVE) convertDoc(rec) match {
            case Success(doc) => iwriter.addDocument(doc)
            case Failure(_) => Console.err.println(s"Record mfn[${rec.getMfn.toString}] convertion error.")
          }
      }
      iwriter.forceMerge(1)
      iwriter.close()
      directory.close()
    }
  }

  private def convertDoc(rec: Record): Try[Document] = {
    Try {
      val decsId: String = rec.getMfn.toString
      val uniqId: String = Option(rec.getField(480, 1)).map(_.getContent).getOrElse("")
      val descrEn: String = Option(rec.getField(1, 1)).map(_.getContent).getOrElse("")
      val descrEs: String = Option(rec.getField(2, 1)).map(_.getContent).getOrElse("")
      val descrPt: String = Option(rec.getField(3, 1)).map(_.getContent).getOrElse("")
      val descrFr: String = Option(rec.getField(16, 1)).map(_.getContent).getOrElse("")
      val treeNum: List[String] = rec.getFieldList(20).asScala.map(_.getContent).toList
      val scopeNoteEn: String = Option(rec.getField(5, 1)).map(fld => fld.getSubfield('n', 1).getContent).getOrElse("")
      val scopeNoteEs: String = Option(rec.getField(6, 1)).map(fld => fld.getSubfield('n', 1).getContent).getOrElse("")
      val scopeNotePt: String = Option(rec.getField(7, 1)).map(fld => fld.getSubfield('n', 1).getContent).getOrElse("")
      val doc: Document = new Document()

      doc.add(new StringField("unique_id", uniqId, Field.Store.YES))
      doc.add(new StoredField("decs_id", decsId))
      treeNum.foreach(tn => doc.add(new StoredField("tree_number", tn)))
      doc.add(new StoredField("descriptor_en", descrEn))
      doc.add(new StoredField("descriptor_es", descrEs))
      doc.add(new StoredField("descriptor_pt", descrPt))
      doc.add(new StoredField("descriptor_fr", descrFr))
      doc.add(new StoredField("descriptor_en_tok", descrEn))
      doc.add(new StoredField("descriptor_es_tok", descrEs))
      doc.add(new StoredField("descriptor_pt_tok", descrPt))
      doc.add(new StoredField("descriptor_fr_tok", descrFr))
      doc.add(new StoredField("scopeNote_en", scopeNoteEn))
      doc.add(new StoredField("scopeNote_es", scopeNoteEs))
      doc.add(new StoredField("scopeNote_pt", scopeNotePt))

      doc
    }
  }
}