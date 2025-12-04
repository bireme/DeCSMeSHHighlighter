/*=========================================================================

    DeCSMeSHFinder © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHFinder/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmf

import com.github.pemistahl.lingua.api.{Language, LanguageDetector, LanguageDetectorBuilder}
import jakarta.servlet.{ServletConfig, ServletContext}
import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
//import org.apache.tika.metadata.Metadata
//import org.apache.tika.parser.AutoDetectParser
//import org.apache.tika.sax.BodyContentHandler

import java.io.{InputStream, PrintWriter}
import org.bireme.dh.{Config, Highlighter}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.util.{Failure, Success, Try}

/**
 * DeCSMeshHighlighter Servlet
 */
class DMFServlet extends HttpServlet {
  private var highlighter: Highlighter = _
  private var markPrefSuffix: MarkPrefSuffix = _
  private var translate: Translate = _
  private var i18n: I18N = _
  private var ollamaHost: String = _
  private var annifBaseUrl: String = _
  private var annifProjectId_pt: String = _
  private var annifProjectId_es: String = _
  private var annifProjectId_en: String = _

  /**
   * Do initial web app configuration
   * @param config servlet config object
   */
  override def init(config: ServletConfig): Unit = {
    super.init(config)

    val context: ServletContext = config.getServletContext
    val decsPath:String = context.getInitParameter("DECS_PATH")
    val decsPrefSufPath:String = context.getInitParameter("DECS_PREFSUF_PATH")
    val i18nIS: InputStream = context.getResourceAsStream("/i18n.txt")

    highlighter = new Highlighter(decsPath)
    markPrefSuffix = new MarkPrefSuffix(decsPrefSufPath)
    translate = new Translate(decsPath)
    i18n = new I18N(i18nIS)
    ollamaHost = context.getInitParameter("OLLAMA_HOST")
    annifBaseUrl = context.getInitParameter("ANNIF_BASE_URL")
    //annifProjectId = context.getInitParameter("ANNIF_PROJECT_ID")
    annifProjectId_pt = context.getInitParameter("ANNIF_PROJECT_PT_ID")
    annifProjectId_es = context.getInitParameter("ANNIF_PROJECT_ES_ID")
    annifProjectId_en = context.getInitParameter("ANNIF_PROJECT_EN_ID")

    println("DMFServlet is listening ...")
  }

  /**
   * Process get http requisition
   * @param request http request object
   * @param response http response object
   */
  override def doGet(request: HttpServletRequest,
                     response: HttpServletResponse): Unit = processRequest(request, response)

  /**
   * Process post http requisition
   * @param request http request object
   * @param response http response object
   */
  override def doPost(request: HttpServletRequest,
                      response: HttpServletResponse): Unit = processRequest(request, response)

  /**
   *  Process get or post requisition
   * @param request http request object
   * @param response http response object
   */
  private def processRequest(request: HttpServletRequest,
                             response: HttpServletResponse): Unit = {
    request.setCharacterEncoding("UTF-8")
    response.setCharacterEncoding("UTF-8")
    response.setContentType("text/html;charset=UTF-8")

    val headerLang: String = Try(getHeaderLang(request)).getOrElse("pt")

    Try {

      val language: String = Option(request.getParameter("lang")).map(_.trim)
        .map(l => if (l.isEmpty) headerLang else l).getOrElse(headerLang)
      val breakSignal: String = "!__break__!"
      val isFirstLoad: Boolean = Option(request.getParameter("isFirstLoad")).map(_.trim) match {
        case Some(value) => value.toBoolean
        case None => true
      }
      val outLang: Option[String] = Option(request.getParameter("outLang")).map(_.trim)
        .flatMap(par => if (par.isEmpty) None else Some(par))
      val termTypes: Seq[String] = Option(request.getParameter("termTypes")).map(_.trim)
        .map(_.split(" *\\| *").toSeq).getOrElse(Seq[String]("Descriptors", "Qualifiers"))
      val inputText000: String = Option(request.getParameter("inputText")).map(_.trim).getOrElse("")
      //println(s"inputText000=$inputText000")
      val inputText00: String = inputText000.replaceAll("(\r?\n\r?|<br>|<div>|<section>|<article>|<header>|<footer>|<nav>|<aside>|<h1>|<h2>|<h3>|<h4>|<h5>|<h6>|<p>|<pre>|<blockquote>|<ul>|<ol>|<li>|" +
        "<form>|<fieldset>|<legend>|<table>|<caption>|<thead>|<tbody>|<tfoot>|<tr>|<th>|<td>|<figure>|<figcaption>|<hr>|<main>|<address>|<canvas>|<video>)", breakSignal)
      val inputText0: String = if (inputText00.startsWith(breakSignal)) inputText00.substring(breakSignal.length) else inputText00
      //println(s"inputText0=$inputText0")
      val doc: Document = Jsoup.parse(inputText0)
      val inputTextX: String = doc.body().text().trim

      //println(s"inputTextX=$inputTextX")

      val showSR: Boolean = Option(request.getParameter("showSR")).exists(_.toBoolean)
      //println(s"processRequest. showSR=$showSR")
      val useFrequencySort: Boolean = Option(request.getParameter("frequencySort")).forall(_.toBoolean)
      //println("inputLang0=" + request.getParameter("inputLang"))
      lazy val inputLangX: String = Option(request.getParameter("inputLang")).map(_.trim).getOrElse("All languages") match {
        case "" => "All languages"
        case "All languages" =>
          //val detector: LanguageDetector = LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.FRENCH, Language.PORTUGUESE, Language.SPANISH).build()
          val detector: LanguageDetector = LanguageDetectorBuilder.fromAllLanguages().build()
          val detectedLanguage: Language = detector.detectLanguageOf(inputTextX)
          //println(s"detectedLanguage=${detectedLanguage.name()}")
          val lang: String = detectedLanguage.name() match {
            case "ENGLISH" => "en"
            case "FRENCH" => "fr"
            case "PORTUGUESE" => "pt"
            case "SPANISH" => "es"
            case other => other
          }
          //println(s"lang=$lang")
          lang
        case "en" => "en"
        case "fr" => "fr"
        case "pt" => "pt"
        case "es" => "es"
        case _ => "en"
      }

      //require(inputTextX.length < 200000, s"Your text size is greater than 200,000 characters. Size=[${inputTextX.length}]")
      //println(s"inputLang=$inputLang")
      val outputText: String = if (inputTextX.length > 200000) {
        getHtml(inputLang="", outLang=language /*"Same of the text"*/, termTypes,
          markedInputText=s"Your text size is greater than 200,000 characters. Size=[${inputTextX.length}]", "", language,
          srText="", annifText="", exportText="", useFrequencySort=useFrequencySort, isFirstLoad=isFirstLoad)
      } else if (inputTextX.isEmpty) {
        getHtml(inputLang="", outLang=language /*"Same of the text"*/, termTypes, markedInputText="", inputTextX, language,
          srText="", annifText="", exportText="", useFrequencySort=useFrequencySort, isFirstLoad=isFirstLoad)
      } else {
        val (inputLang: String, inputText: String) =
          if (inputLangX.equals("en") || inputLangX.equals("es") || inputLangX.equals("pt") || inputLangX.equals("fr")) (inputLangX, inputTextX)
          else {
            /*val ollamaClient: OllamaClient = new OllamaClient(ollamaHost, None)
            val translatedText: String = translateText(ollamaClient, inputTextX.replace(breakSignal, "\n"), textLanguage = inputLangX.toLowerCase, targetLanguage = language)
            println(s"textLanguage = $inputLangX, targetLanguage = $language translatedText = $translatedText")
            (language, translatedText.replace("\n", breakSignal))*/
            ("All languages", inputTextX)
          }
        //println(s"inputLang=$inputLang inputText=$inputText")
        val containsDescriptors: Boolean = termTypes.contains("Descriptors")
        val config = Config(
          scanLang = Some(inputLang), outLang = outLang, scanMainHeadings = containsDescriptors, scanEntryTerms = true,
          scanQualifiers = termTypes.contains("Qualifiers"), scanPublicationTypes = containsDescriptors,
          scanCheckTags = containsDescriptors, scanGeographics = containsDescriptors
        )
        //println(s"inputLang=$inputLang outLang=$outLang termTypes=$termTypes inputText=[$inputText] language=$language useFrequencySort=$useFrequencySort containsDescriptor=$containsDescriptors config=$config")
        val oLanguage: String = outLang match { //.getOrElse(inputLang.getOrElse("en"))
          case Some(lang) => if (lang.length == 2) lang else inputLang
          case None => inputLang
        }
        val descriptors: (String, Seq[(Int, Int, String, String, String, String)], Seq[(String, Int, Double)]) =
          highlighter.highlight(markPrefSuffix.prefSuffix(_, _, termLang = oLanguage, tipLang = language), inputText, config)
        //println(s"descriptors=$descriptors")
        //highlighter.highlight("[", "]",  inputText, config)
        //println(s"descriptors=${descriptors._1}")
        val srText: String = ""/*{
          if (showSR) {
            val ollamaClient: OllamaClient = new OllamaClient(ollamaHost, None)
            val ollamaModel: Option[String] = inputLang match {
              case "pt" => Some("sr-v1-pt")
              case "es" => Some("sr-v1-es")
              case "en" => Some("sr-v1-en")
              case _ => None
            }
            val inputTextLen: Int = inputText.length
            if (ollamaModel.isEmpty || (inputTextLen < 300) || (inputTextLen > 2300))
              //i18n.translate("SS Generation", language)
              inputText
            else ollamaClient.chat(inputText, ollamaModel.get) match {
              case Success(value) => translateText(ollamaClient, value, textLanguage = inputLang, language)
              case Failure(exception) => s"SuperResumos error: ${exception.getMessage}"
            }
          } else ""
        }*/
        val annif: AnnifClient = new AnnifClient(annifBaseUrl)
        val annifSuggestions: Either[String, Seq[AnnifSuggestion]] = inputLang match {
          case "pt" => annif.getSuggestions(annifProjectId_pt, inputText, limit = Some(7), threshold=Some(0.13f))
          case "es" => annif.getSuggestions(annifProjectId_es, inputText, limit = Some(7), threshold=Some(0.13f))
          case "en" => annif.getSuggestions(annifProjectId_en, inputText, limit = Some(7), threshold=Some(0.13f))
          case _ => Right(Seq[AnnifSuggestion]())
        }
        //val annifSuggestions: Either[String, Seq[Suggestion]] = Right(Seq[Suggestion]())
        val annifTerms0: Seq[(String, Option[String])] = getAnnifTerms(annifSuggestions, Some(inputLang), Some(oLanguage))
          .getOrElse(Seq[(String, Option[String])]())
        val annifTerms: Seq[(String, Option[String])] = annifTerms0.map {
          case (label, notation) =>
            val notat: Option[String] = notation.map {
              n =>
                n.indexOf("|") match {
                  case -1 => n
                  case pos => n.substring(0, pos)
                }
            }
            (label, notat)
        }
        val annifTermsPrefSuf: Seq[String] = annifTerms.map(term => markPrefSuffix.prefSuffix1(term._1, termLang = oLanguage, tipLang = language))
        println(s"annifSuggestions=$annifSuggestions\n\nannifTerms0=$annifTerms0\n\nannifTerms=$annifTerms\n\nannifTermsPrefSuf=$annifTermsPrefSuf\n\n")
        val annifZip: Seq[(String, Float)] =  annifTermsPrefSuf.zip(annifSuggestions.getOrElse(Seq[AnnifSuggestion]()).map(_.score))
        val annifZip1: Seq[String] = annifZip.map {
          case (key, score) =>
             score match {
               case x if x >= 0.5  => s"🟢 $key"
               case x if x >= 0.09 => s"🟡 $key"
               case _              => s"⭕ $key"
             }
        }
        //val descr: Seq[String] = descriptors._3.map(_._1)
        val descr: Set[(String,String)] = descriptors._2.map(t => (t._3, t._5)).toSet
        val exportText: String = getExportTermsText(descr, annifTerms, language)

        //inputText.foreach(ch => println(s"[$ch]=${ch.toInt}"))
        //println(s"Antes de chamar o getHtml. srText=[$srText]")
        /*getHtml(inputLang, oLanguage, termTypes, descriptors._1.replace(breakSignal, "<br/>"), inputText.replace("\n", "<br/>"),
          language, srText, annifTermsPrefSuf.mkString("<br>"), exportText, useFrequencySort, isFirstLoad)*/

        /*getHtml(inputLang, oLanguage, termTypes, descriptors._1.replace(breakSignal, "<br/>"), inputText.replace("\n", "<br/>"),
          language, srText, annifTermsPrefSuf.mkString("<br>"), exportText, useFrequencySort, isFirstLoad)*/
        getHtml(inputLang, oLanguage, termTypes, descriptors._1.replace(breakSignal, "<br/>"), inputText.replace("\n", "<br/>"),
          language, srText, annifZip1.mkString("<br>"), exportText, useFrequencySort, isFirstLoad)
      }
      //println(s"markedInputText=[${descriptors._1.replace(breakSignal, "<br/>")}]")
      val out: PrintWriter = response.getWriter
      out.println(outputText)
      out.flush()
      //println("======================================================================================")
    } match {
      case Success(_) => ()
      case Failure(exception: Throwable) =>
        exception.printStackTrace()
        val errMess: String = i18n.translate("SS Generation", headerLang)
        val outputText: String = getHtml(inputLang=headerLang, outLang=headerLang, Seq[String]("Descriptors", "Qualifiers"), markedInputText=errMess,
          originalInputText="", language=headerLang, srText="", annifText="", exportText="", useFrequencySort=true, isFirstLoad=false)
        //println(s"===> outputText = [$outputText]")
        val out: PrintWriter = response.getWriter
        out.println(outputText)
        out.flush()
        //response.sendError(500, errMess)
    }
  }

  private def getAnnifTerms(annifSuggestions: Either[String, Seq[AnnifSuggestion]],
                            inputLang: Option[String],
                            outputLang: Option[String]): Either[String, Seq[(String, Option[String])]] = {
    val annifTerms: Either[String, Seq[(String, Option[String])]] = annifSuggestions match {
      case Right(suggestions) => inputLang match {
        case Some(iLang) =>
          val iLang2: String = if (iLang.equals("All languages")) "en" else iLang
          val outLang: String = outputLang match {
            case Some(oLang) => if (oLang.equals("Same of the text")) iLang2 else oLang
            case None => iLang
          }
          val res: Seq[(String, Option[String])] = suggestions.foldLeft(Seq[(String, Option[String])]()) {
            case (seq, suggestion) =>
              translate.translate(suggestion.label, outLang) match {
                case Right(translated) => seq.appended((translated, suggestion.notation))
                case Left(_) => seq
              }
          }
          Right(res)
        case None => Right(Seq[(String, Option[String])]())
      }
      case Left(error) => Left(error)
    }
    annifTerms
  }

  private def getExportTermsText(descriptors: Set[(String, String)],
                                 annifTerms: Seq[(String, Option[String])],
                                 language: String): String = {
    val buffer = new StringBuilder()

    buffer.append(s"=== ${i18n.translate("Extracted descriptors", language)} ===")
    descriptors.foreach { case (id, descr) => buffer.append(s"\\n$descr [${id.toUpperCase()}]")}

    buffer.append(s"\\n\\n=== ${i18n.translate("Terms identified by AI", language)} ===")
    annifTerms.foreach { case (descr, id) => buffer.append(s"\\n$descr [${id.getOrElse("").toUpperCase()}]")}
    buffer.toString()
  }

  /**
   *
   * @param request HttpServletRequest object
   * @return the desired input/output language according to the request header Accept-Language
   */
  private def getHeaderLang(request: HttpServletRequest): String = {
    //println(s"Accept-Language=${request.getHeader("Accept-Language")}")
    val header = Option(request.getHeader("Accept-Language")).map(_.toLowerCase).getOrElse("pt")
    val langs: Array[String] = header.split(",|;")

    langs.find {
      lang => lang.equals("fr") || lang.equals("en") || lang.equals("pt") || lang.equals("es")
    }.getOrElse("pt")
  }

  private def translateText(ollamaClient: OllamaClient,
                            text: String,
                            textLanguage: String,
                            targetLanguage: String): String = {
    val languages: Set[String] = Set("pt", "en", "es", "fr")
    //println(s"entrando no translateText textLanguage=$textLanguage targetLanguage=$targetLanguage")
    if (text.trim.isEmpty|| !languages.contains(targetLanguage) || targetLanguage.equals(textLanguage)) text
    else {
      //println(s"2 - entrando no translateText textLanguage=$textLanguage targetLanguage=$targetLanguage")
      val idioma: String = targetLanguage match {
        case "pt" => "portugues"
        case "es" => "espanhol"
        case "en" => "ingles"
        case _ => "frances"
      }
      val prompt: String = s"\n\nDado o seguinte texto de entrada traduza-o para o $idioma retornando como resposta unicamente o texto traduzido. Mantenha as mesmas quebras de linha do texto: $text\n\n"

      //println(s"prompt=$prompt")
      ollamaClient.chat(prompt, "llama3.2") match {
        case Success(output) =>
          output.indexOf(":") match {
            case i if i <= 0 => output
            case i =>
              val out: String = output.substring(i + 1).trim
              if (out.head == '"' && out.length > 2) out.substring(1, out.length - 1)
              else out
          }
        case Failure(exception) =>
          exception.printStackTrace()
          //println(exception.toString)
          text
      }
    }
  }

  private def getHtml(inputLang: String,          // Language of the text entered by user
                      outLang: String,            // Language of the terms to be used in the found terms
                      termTypes: Seq[String],     // Descriptors and/or Qualifiers
                      markedInputText: String,    // Text entered by the user and with its terms marked with tooltips
                      originalInputText: String,  // Text entered by the user
                      language: String,           // Language used in the interface
                      srText: String,             // SuperResumos text
                      annifText: String,          // Terms identified by Annif and marked with tooltip
                      exportText: String,         // Text put in the export file
                      useFrequencySort: Boolean,
                      isFirstLoad: Boolean): String = {

    //println(s"markedInputText=[${Option(markedInputText).getOrElse("<vazio>")}]")
    //println(s"originalInputText=[${Option(originalInputText).getOrElse("<vazio>")}]")

    val warningMessage: String = language match {
      case "en" => "Find ideal descriptors with DeCS Finder.\nSimplify the indexing of your texts with artificial intelligence. DeCS Finder, developed by BIREME, identifies DeCS/MeSH descriptors for your content quickly and precisely.\nTry it now and boost your search!"
      case "es" => "Encuentre los descriptores ideales con DeCS Finder.\nSimplifique la indización de sus textos con inteligencia artificial. DeCS Finder, desarrollado por BIREME, identifica descriptores DeCS/MeSH para su contenido de forma rápida y precisa.\n¡Pruébelo ahora y potencie su búsqueda!"
      case "pt" => "Encontre descritores ideais com o DeCS Finder.\nSimplifique a indexação dos seus textos com inteligência artificial. O DeCS Finder, desenvolvido pela BIREME, identifica os descritores DeCS/MeSH para o seu conteúdo, de forma rápida e precisa.\nExperimente agora e potencialize sua pesquisa!"
      case "fr" => "Trouvez les descripteurs idéaux avec DeCS Finder.\nSimplifiez l'indexation de vos textes grâce à l'intelligence artificielle. DeCS Finder, développé par BIREME, identifie rapidement et précisément les descripteurs DeCS/MeSH de vos contenus.\nEssayez-le maintenant et boostez votre recherche!"
      case _ => "Find ideal descriptors with DeCS Finder.\nSimplify the indexing of your texts with artificial intelligence. DeCS Finder, developed by BIREME, identifies DeCS/MeSH descriptors for your content quickly and precisely.\nTry it now and boost your search!"
    }

    s"""
<!DOCTYPE html>
<html lang="""" + language + """">

<head>
  <!-- script type="text/javascript">
    console.log("começo do head");
  </script -->

  <!-- script type="text/javascript"> alert(`inputLang="${inputLang}"\n\n outLang="${outLang}"\n\n termTypes="${termTypes}"\n\n markedInputText="${markedInputText}"\n\n originalInputText="${originalInputText}" \n\n language="${language}"\n\n annifText="${annifText}"\n\n exportText="${exportText}"\n\n useFrequencySort="${useFrequencySort}"\n\n isFirstLoad="${isFirstLoad}"`)</script -->

  <!-- Google tag (gtag.js) - Google Analytics -->
        <script async src="https://www.googletagmanager.com/gtag/js?id=G-DBPY4Q6HT8"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());

            gtag('config', 'G-DBPY4Q6HT8');
        </script>

	<meta charset="UTF-8"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="autor" content=" BIREME | OPAS | OMS - > Márcio Alves"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>DeCSMeSH Finder - Advanced</title>
	<link rel="stylesheet" href="decsf/css/bootstrap.min.css"/>
	<link rel="stylesheet" href="decsf/css/fontawesome/css/all.css"/>
	<link rel="stylesheet" href="decsf/css/bootstrap-select.css"/>
	<link rel="stylesheet" href="decsf/css/accessibility.css"/>
	<link rel="stylesheet" href="decsf/css/style.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
  <link rel="shortcut icon" href="decsf/img/favicon.png"/>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/FileSaver.js/2.0.5/FileSaver.min.js"></script>

  <style>
      /* Estilos para a caixa de diálogo */
      #modal {
          display: none;
          position: fixed;
          left: 0;
          top: 0;
          width: 100%;
          height: 100%;
          background-color: rgba(0, 0, 0, 0.5);
          justify-content: center;
          align-items: center;
      }

      #modal-content {
          background-color: #669966;
          padding: 20px;
          border-radius: 5px;
          text-align: center;
          color: white;
          max-width: 100ch;
      }

      #close-btn {
          margin-top: 10px;
      }
    </style>

    <style>
      #textWithTooltips {
        display: block;
        width: 100%;
        height: """ + (if (srText.isEmpty) "calc(100vh - 406px);" else "calc(100vh - 575px);") + """
        padding: .375rem .75rem;
        font-size: 1rem;
        font-weight: 400;
        line-height: 1.5;
        color: #495057;
        background-color: #fff;
        background-clip: padding-box;
        border: 1px solid #ced4da;
        border-radius: .25rem;
        transition: border-color .15s ease-in-out, box-shadow .15s ease-in-out;
        overflow-x: auto;
      }
      #textWithTooltips:focus-visible {
        background-color: #fff;
        border-color: #80bdff;
        outline: 0;
        box-shadow: 0 0 0 .2rem rgba(0, 123, 255, .25);
      }
      #textWithTooltips a {
        cursor: pointer;
      }

      #textWithTooltipsAnnif {
        display: block;
        width: 100%;
        height: calc(100vh - 406px);
        padding: .375rem .75rem;
        font-size: 1rem;
        font-weight: 400;
        line-height: 1.5;
        color: #495057;
        background-color: #fff;
        background-clip: padding-box;
        border: 1px solid #ced4da;
        border-radius: .25rem;
        transition: border-color .15s ease-in-out, box-shadow .15s ease-in-out;
        overflow-x: auto;
      }
      #textWithTooltipsAnnif:focus-visible {
        background-color: #fff;
        border-color: #80bdff;
        outline: 0;
        box-shadow: 0 0 0 .2rem rgba(0, 123, 255, .25);
      }
      #textWithTooltipsAnnif a {
        cursor: pointer;
      }

       #superResumos {
        font-size: 1rem;
        font-weight: 400;
        line-height: 1.5;
        color: #495057;
        background-color: #fff;
        background-clip: padding-box;
        border: 1px solid #ced4da;
        border-radius: .25rem;
        transition: border-color .15s ease-in-out, box-shadow .15s ease-in-out;
        overflow-x: auto;
      }
      #textWithTooltips:focus-visible {
        background-color: #fff;
        border-color: #80bdff;
        outline: 0;
        box-shadow: 0 0 0 .2rem rgba(0, 123, 255, .25);
      }
      #textWithTooltips a {
        cursor: pointer;
      }
    </style>

    <style>
        .tooltip-inner {
            background-color: /*#669966*/ #1e491f !important; /* Fundo verde */
            color: #ffffff !important;          /* Texto branco */
            max-width: 600px; /* Define o novo tamanho máximo */
        }

        .tooltip .arrow::before {
            background-color: #f39c12 !important; /* Cor da seta */
        }
    </style>

    <style>
      #header img {
        margin-top: 10px;
      }

      #header {
        padding-bottom: 10px;
      }
    </style>

    <style>
      .dialog-overlay {
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: none; /* Agora invisível por padrão */
        justify-content: center;
        align-items: center;
        z-index: 1000;
      }
      .dialog-box {
        background: white;
        padding: 1.5em;
        border-radius: 8px;
        width: 300px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
      }
      .dialog-buttons {
        margin-top: 1em;
        display: flex;
        justify-content: space-between;
      }
    </style>

  <script type="text/javascript">
    window.handleXXX1 = function(event) {
        alert("entrando no handleXXX1. " + event);
    }
  </script>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.4.120/pdf.min.js"></script>

  <script type="text/javascript">
    window.handleFChange = async function(event) {
      //alert("Entrando no handleFChange.");

      const input = event.target;
      if (!input.files || input.files.length === 0) {
        //alert("Nenhum arquivo selecionado.");
        document.body.style.cursor = "default";
        return;
      }

      const file = input.files[0];
      const isPdf = /pdf$/i.test(file.type) || /\.pdf$/i.test(file.name);
      const isText = /plain$/i.test(file.type) || /\.(txt|text)$/i.test(file.name);

      let text = "";

      //alert("isPdf = " + isPdf);

      if (isPdf) {
        try {
          const buffer = await file.arrayBuffer();
          const pdf = await pdfjsLib.getDocument({ data: buffer }).promise;

          for (let p = 1; p <= pdf.numPages; p++) {
            const page = await pdf.getPage(p);
            const content = await page.getTextContent();
            const pageText = content.items.map(item => item.str).join(" ");
            text += pageText + "\n";
          }
        } catch (err) {
          alert("Error reading PDF: " + err);
          document.body.style.cursor = "default";
          return;
        }
      } else {
        if (isText) {
          try {
            text = await file.text();
          } catch (err) {
            alert("Error reading text: " + err);
            document.body.style.cursor = "default";
            return;
          }
        } else {
            alert("Only text and pdf files are allowed.");
            document.body.style.cursor = "default";
            return;
        }
      }

      //alert("Conteúdo do arquivo:\n" + text);
      var textWithTooltips = document.getElementById("textWithTooltips");
	    textWithTooltips.textContent = text;
      var inputTextLanguage = document.getElementById('inputTextLanguage')
      inputTextLanguage.value = "All languages"

      submitPage(``, """" + language + """", "false");
    };
  </script>

  <script>
  function showDialog() {
    document.getElementById('custom-dialog').style.display = 'flex';
  }

  function handleOpen() {
    const url = document.getElementById('url-input').value;
    document.getElementById('custom-dialog').style.display = 'none';

    //alert("Abrir: " + url);
    if (url.length > 0) {
      //alert("Vou chamar fetchTextOrPDFContent");
      fetchTextOrPDFContent(url)
      .then(conteudo => {
        //alert("conteúdo = " + conteudo);
        var textWithTooltips = document.getElementById("textWithTooltips");
	      textWithTooltips.textContent = conteudo.substring(0, 190000);

        submitPage(``, """" + inputLang + """", "false");
      })
      .catch(err => {
        alert("Download error: " + err);
      });
    } else {
      alert("empty url");
    }
  }

  function handleCancel() {
    document.getElementById('custom-dialog').style.display = 'none';
  }

  async function fetchTextOrPDFContent(targetUrl) {
    const proxyUrl = 'https://corsproxy.io/?';
    const fullUrl = proxyUrl + encodeURIComponent(targetUrl);

    try {
      const response = await fetch(fullUrl, { method: 'GET' });

      if (!response.ok) {
        console.error("Erro na resposta:", response);
        throw new Error(`Erro ao acessar a URL: ${response.status} ${response.statusText}`);
      }

      const contentType = response.headers.get('Content-Type') || '';
      //console.log("Tipo de conteúdo:", contentType);

      if (contentType.includes('application/pdf') || targetUrl.endsWith(".pdf")) {
        return await extractTextFromPDF(response);
      }

      if (
        contentType.includes('text/plain') ||
        //contentType.includes('text/html') ||
        contentType.includes('application/json')
      ) {
        return await response.text();
      }

      alert("Formato não suportado [" + contentType + "]. Apenas arquivos texto ou PDF são aceitos.");
      throw new Error("Formato não suportado. Apenas arquivos texto ou PDF são aceitos.");

    } catch (error) {
      console.error("Erro ao buscar conteúdo:", error);
      return '';
    }
  }

  async function extractTextFromPDF(response) {
    try {
      const blob = await response.blob();
      const pdfData = await blob.arrayBuffer();

      const pdfjsLib = await import('https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.2.67/pdf.mjs');
      const loadingTask = pdfjsLib.getDocument({ data: pdfData });
      const pdf = await loadingTask.promise;

      let text = '';
      for (let i = 1; i <= pdf.numPages; i++) {
        const page = await pdf.getPage(i);
        const content = await page.getTextContent();
        const pageText = content.items.map(item => item.str).join(' ');
        text += `\n--- Página ${i} ---\n${pageText}\n`;
      }

      return text;

    } catch (error) {
      console.error("Erro ao processar PDF:", error);
      throw new Error("Não foi possível extrair texto do PDF.");
    }
}

  </script>

  <script src="decsf/js/DeCSFinder.js"></script>

  <!-- script type="text/javascript">
    console.log("fim do head");
  </script -->

   <style>
      /* 1. html e body com 100% de altura */
      html, body {
        height: 100%;
        margin: 0;
      }

      /* 2. body como flex container coluna */
      body {
        display: flex;
        flex-direction: column;
      }

      /* 3. region de conteúdo principal cresce para preencher */
      #main_container {
        flex: 1;
      }

      /* 4. footer sem flex-grow */
      #barAccessibility {
        /* opcional: estilos de cor, padding etc */
      }
    </style>

</head>

<body>

  <!-- script type="text/javascript">
    console.log("começo do body");
  </script -->

   <div id="main_container">
      <!-- ACESSIBILITY AREA -->
     """ + Accessibility.accessibilityArea(language, i18n) + """

      <!-- HEADER -->
      """ + Header.headerArea(originalInputText, language) + """

      <!-- LANGUAGE AREA -->
      """ + LanguageTags.languageArea(inputLang, outLang, termTypes, language, i18n) + """

      <!-- MAIN AREAS -->
      """ + MainAreas.inputOutputAreas("Paste your text below", markedInputText.trim, originalInputText, srText, annifText, exportText, language, i18n) + """
  </div>
  <!-- FOOTER -->
  """ + Footer.footer(language, i18n) + """

  <script>
        // Captura o evento de mudança no select
        document.getElementById('inputTextLanguage').addEventListener('change', function() {
            //alert("evento de mudança no select do inputTextLanguage");
            // Obtém o valor selecionado
            var selectedLanguage = this.value;

            // Verifica se o Google Analytics está disponível
            if (typeof gtag === 'function') {
                // Envia o evento para o Google Analytics
                gtag('event', 'language_selection', {
                    'event_category': 'interaction',
                    'event_label': 'Language of your text',
                    'value': selectedLanguage
                });
            } else {
                console.error('Google Analytics não está disponível.');
            }
            // Resubmit pagina para atualizar a mudança da lingua
            submitPage(`""" + originalInputText + """`, """" + language + """", "false");
        });
  </script>

  <script>
        // Captura o evento de mudança no select
        document.getElementById('outputTextLanguage').addEventListener('change', function() {
            // Obtém o valor selecionado
            var selectedLanguage = this.value;

            // Verifica se o Google Analytics está disponível
            if (typeof gtag === 'function') {
                // Envia o evento para o Google Analytics
                gtag('event', 'language_selection', {
                    'event_category': 'interaction',
                    'event_label': 'Language of the descriptors',
                    'value': selectedLanguage
                });
            } else {
                console.error('Google Analytics não está disponível.');
            }
            // Resubmit pagina para atualizar a mudança da lingua
            submitPage(`""" + originalInputText + """`, """" + language + """", "false");
        });
  </script>

  <script>
      // Captura o evento de mudança no select
      document.getElementById('termTypes').addEventListener('change', function() {
          // Obtém todas as opções selecionadas
          var selectedOptions = Array.from(this.selectedOptions).map(option => option.value);

          // Verifica se o Google Analytics está disponível
          if (typeof gtag === 'function') {
              // Envia o evento para o Google Analytics
              gtag('event', 'type_terms_selection', {
                  'event_category': 'interaction',
                  'event_label': selectedOptions.join(', '), // Lista de valores selecionados
                  'value': selectedOptions.length // Número de seleções feitas
              });
          } else {
              console.error('Google Analytics não está disponível.');
          }
          // Resubmit pagina para atualizar a mudança da lingua
          submitPage(`""" + originalInputText + """`, """" + language + """", "false");
      });
  </script>

  <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="exampleModalLabel">DeCS Finder</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">""" + warningMessage + """</div>
      </div>
    </div>
  </div>

  <script src="decsf/js/jquery-3.4.1.min.js"></script>
	<script src="decsf/js/bootstrap.bundle.min.js"></script>
	<script src="decsf/js/bootstrap-select.js"></script>
	<script src="decsf/js/cookie.js"></script>
	<script src="decsf/js/accessibility.js"></script>
	<script src="decsf/js/main.js"></script>

  """ + getFirstLoadText(isFirstLoad) + """
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Inicializar todos os tooltips
        document.addEventListener('DOMContentLoaded', function () {
            const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
            tooltipTriggerList.forEach(function (tooltipTriggerEl) {
                new bootstrap.Tooltip(tooltipTriggerEl);
            });
        });
    </script>

    <script>
      //alert("Entrando no script do textWithTooltips");
      const el = document.getElementById('textWithTooltips');

      el.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          event.preventDefault(); // evita quebra de linha
          const text = el.innerText.trim();
          if (text !== "") {
            document.body.style.cursor = "wait";
            submitPage(`""" + originalInputText + """`, """" + language + """", "false");
          }
        }
      });

      el.addEventListener('paste', (e) => {
        // extrai apenas o texto colado
        const pastedText = e.clipboardData.getData('text');
        //alert("Pasted text=" + pastedText);
        onPaste(el, pastedText);
      });

      function onPaste(el, text) {
        //alert('Texto colado:' + text);
        // coloque aqui sua lógica
        el.innerHTML = text;
        document.body.style.cursor = "wait";
        submitPage(`""" + originalInputText + """`, """" + language + """", "false");
      }
    </script>
</body>
</html>
    """
  }

  private def getFirstLoadText(isFirstLoad: Boolean): String = {
    if (isFirstLoad) { """
      <script>
        $(document).ready(function () {
          $('#myModal').modal('show');
        });
        document.querySelector('.close').addEventListener('click', function () {
        $('#myModal').modal('hide');
        });
      </script>"""
    } else ""
  }

  /*private def extractDocOdtText(): Try[String] = {
    Try {
      val parser: AutoDetectParser = new AutoDetectParser()
      val handler: BodyContentHandler = new BodyContentHandler()
      val metadata: Metadata = new Metadata()
      val stream =

      parser.parse(stream, handler, metadata)
      handler.toString()
    }
  } */
}
