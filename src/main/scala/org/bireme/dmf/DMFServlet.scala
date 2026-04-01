/*=========================================================================

    DeCSMeSHFinder © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHFinder/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmf

import jakarta.servlet.{ServletConfig, ServletContext}
import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
//import org.apache.tika.metadata.Metadata
//import org.apache.tika.parser.AutoDetectParser
//import org.apache.tika.sax.BodyContentHandler

import com.github.pemistahl.lingua.api.{Language, LanguageDetector, LanguageDetectorBuilder}

import java.io.{InputStream, PrintWriter}
import org.bireme.dh.{Config, Highlighter}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scalatags.Text
import scalatags.Text.all._

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
      val translateRequested: Boolean = Option(request.getParameter("translateRequested")).exists(_.toBoolean)
      val translateSourceLang: Option[String] = Option(request.getParameter("translateSourceLang")).map(_.trim).filter(_.nonEmpty)
      val isFirstLoad: Boolean = Option(request.getParameter("isFirstLoad")).map(_.trim) match {
        case Some(value) => value.toBoolean
        case None => true
      }
      val outLang: Option[String] = Option(request.getParameter("outLang")).map(_.trim)
        .flatMap(par => if (par.isEmpty) None else Some(par))
      val termTypes: Seq[String] = /*Option(request.getParameter("termTypes")).map(_.trim)
        .map(_.split(" *\\| *").toSeq).getOrElse(Seq[String]("Descriptors", "Qualifiers")) */ Seq[String]("Descriptors", "Qualifiers") 
      val inputText000: String = Option(request.getParameter("inputText")).map(_.trim).getOrElse("")
      //println(s"inputText000=$inputText000")
      val inputText00: String = inputText000.replaceAll("(\r?\n\r?|<br>|<div>|<section>|<article>|<header>|<footer>|<nav>|<aside>|<h1>|<h2>|<h3>|<h4>|<h5>|<h6>|<p>|<pre>|<blockquote>|<ul>|<ol>|<li>|" +
        "<form>|<fieldset>|<legend>|<table>|<caption>|<thead>|<tbody>|<tfoot>|<tr>|<th>|<td>|<figure>|<figcaption>|<hr>|<main>|<address>|<canvas>|<video>)", breakSignal)
      val inputText0: String = if (inputText00.startsWith(breakSignal)) inputText00.substring(breakSignal.length) else inputText00
      //println(s"inputText0=$inputText0")
      val doc: Document = Jsoup.parse(inputText0)
      val inputTextX: String = doc.body().text().trim

      println(s"inputTextX=$inputTextX")

      val showSR: Boolean = Option(request.getParameter("showSR")).exists(_.toBoolean)
      //println(s"processRequest. showSR=$showSR")
      val useFrequencySort: Boolean = Option(request.getParameter("frequencySort")).forall(_.toBoolean)
      val requestedInputLang: String = InputProcessing.normalizeSelectedLanguage(
        Option(request.getParameter("inputLang")).map(_.trim).getOrElse(InputProcessing.AutoDetectLanguage)
      )
      lazy val detectedInputLang: String = detectInputLanguage(inputTextX.replace(breakSignal, " "))
      lazy val inputLangX: String = InputProcessing.resolveInputLanguage(requestedInputLang, detectedInputLang)

      //require(inputTextX.length < 200000, s"Your text size is greater than 200,000 characters. Size=[${inputTextX.length}]")
      //println(s"inputLang=$inputLang")
      val outputText: String = if (inputTextX.length > 200000) {
        getHtml(inputLang="", outLang=language /*"Same of the text"*/, termTypes,
          markedInputText=s"Your text size is greater than 200,000 characters. Size=[${inputTextX.length}]", "", language,
          srText="", annifText="", exportText="", useFrequencySort=useFrequencySort, isFirstLoad=isFirstLoad, translateButtonLocked=false)
      } else if (inputTextX.isEmpty) {
        getHtml(inputLang="", outLang=language /*"Same of the text"*/, termTypes, markedInputText="", inputTextX, language,
          srText="", annifText="", exportText="", useFrequencySort=useFrequencySort, isFirstLoad=isFirstLoad, translateButtonLocked=false)
      } else {
        val translateSourceResolved: Option[String] =
          if (translateRequested) {
            Some(translateSourceLang match {
              case Some(sourceLang) => InputProcessing.resolveInputLanguage(sourceLang, detectedInputLang)
              case None => detectedInputLang
            })
          } else {
            None
          }

        val preparedInput: Either[String, InputProcessing.PreparedInput] =
          InputProcessing.prepareInputForProcessing(
            inputText = inputTextX,
            currentInputLang = inputLangX,
            interfaceLanguage = language,
            translateRequested = translateRequested,
            translateSourceLang = translateSourceResolved,
            translateFn = (text: String, sourceLang: String, targetLang: String) => {
              val ollamaClient = new OllamaClient(ollamaHost, None)
              translateTextEither(
                ollamaClient = ollamaClient,
                text = text.replace(breakSignal, "\n"),
                textLanguage = sourceLang,
                targetLanguage = targetLang
              ).map(_.replace("\n", breakSignal))
            }
          )

        preparedInput match {
          case Left(_) =>
            getHtml(
              inputLang = inputLangX,
              outLang = outLang.getOrElse(language),
              termTypes = termTypes,
              markedInputText = inputTextX.replace(breakSignal, "<br/>"),
              originalInputText = inputTextX.replace(breakSignal, "<br/>"),
              language = language,
              srText = "",
              annifText = "",
              exportText = "",
              useFrequencySort = useFrequencySort,
              isFirstLoad = isFirstLoad,
              translateButtonLocked = translateRequested
            )
          case Right(prepared) => {
            val inputLang: String = prepared.inputLang
            val inputText: String = prepared.inputText
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
            //println(s"prefSuffix=${markPrefSuffix.prefSuffix(_, _, termLang = oLanguage, tipLang = language)}")
            //println(s"text=${descriptors._1}")
            //println(s"descriptors=$descriptors inputText=$inputText config=$config")
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
            //println(s"antes de chamar o getSuggestions. inputLang=[$inputLang]")
            val annifSuggestions: Either[String, Seq[AnnifSuggestion]] = inputLang match {
              /*
              case "pt" => annif.getSuggestions(annifProjectId_pt, inputText, limit = Some(7), threshold=Some(0.13f))
              case "es" => annif.getSuggestions(annifProjectId_es, inputText, limit = Some(7), threshold=Some(0.13f))
              case "en" => annif.getSuggestions(annifProjectId_en, inputText, limit = Some(7), threshold=Some(0.13f))
              */
              case "pt" => annif.getSuggestions(annifProjectId_pt, inputText, limit = Some(15))
              case "es" => annif.getSuggestions(annifProjectId_es, inputText, limit = Some(15))
              case "en" => annif.getSuggestions(annifProjectId_en, inputText, limit = Some(15))
              case "fr" =>
                val inputTextEn: String = translate.translate(inputText, "en") match {
                  case Right(translated) => translated
                  case Left(emsg) =>
                    println(s"Translation to english error: $emsg")
                    ""
                }
                annif.getSuggestions(annifProjectId_en, inputTextEn)
              case _ => Right(Seq[AnnifSuggestion]())
            }
            //println(s"+++annifSuggestions=$annifSuggestions")
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
            //println(s"annifSuggestions=$annifSuggestions\n\nannifTerms0=$annifTerms0\n\nannifTerms=$annifTerms\n\nannifTermsPrefSuf=$annifTermsPrefSuf\n\n")
            val annifZip: Seq[(String, Int)] = annifTermsPrefSuf.zip(annifSuggestions.getOrElse(Seq[AnnifSuggestion]()).map(sc => (sc.score * 100).toInt))

            val annifTermScore: Seq[(String, Int)] = annifTermsPrefSuf.zip(annifZip.map(_._2))
            val annifText: String = prepareAnnifText(annifTermScore)
            val descr: Set[(String, String)] = descriptors._2.map(t => (t._3, t._5)).toSet
            //println(s"descr=$descr")
            val exportText: String = getExportTermsText(descr, annifTerms, language)
            getHtml(
              inputLang,
              oLanguage,
              termTypes,
              descriptors._1.replace(breakSignal, "<br/>"),
              prepared.originalInputText.replace("\n", "<br/>"),
              language,
              srText,
              annifText,
              exportText,
              useFrequencySort,
              isFirstLoad,
              translateRequested
            )
          }
        }
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
          originalInputText="", language=headerLang, srText="", annifText="", exportText="", useFrequencySort=true, isFirstLoad=false,
          translateButtonLocked=false)
        //println(s"===> outputText = [$outputText]")
        val out: PrintWriter = response.getWriter
        out.println(outputText)
        out.flush()
        //response.sendError(500, errMess)
    }
  }

  private def prepareAnnifText(terms: Seq[(String, Int)]): String = {
    def clampScore(v: Int): Int = math.max(0, math.min(100, v))

    terms.map { case (linkHtml, score0) =>
      val score = clampScore(score0)

      /*s"""
         |<div class="d-flex align-items-center" style="margin-bottom: 6px;">
         |  <div class="progress"
         |       style="width: 7mm; height: 0.8rem; flex: 0 0 auto; margin-top: 1px; margin-right: 14px;"
         |       title="Score: $score">
         |    <div class="progress-bar"
         |         role="progressbar"
         |         style="width: $score%; padding: 0; line-height: 0;">
         |    </div>
         |  </div>
         |  <div style="flex: 1 1 auto; line-height: 1.1; margin: 0;">
         |    $linkHtml
         |  </div>
         |</div>
         |""".stripMargin.trim*/

      s"""
         |<div class="d-flex align-items-center" style="margin-bottom: 6px;">
         |  <div class="progress"
         |       style="width: 7mm; height: 0.8rem; flex: 0 0 auto; margin-top: 1px; margin-right: 14px;"
         |       title="Score: $score">
         |    <div class="progress-bar"
         |         role="progressbar"
         |         style="width: $score%; padding: 0; line-height: 0; background-color: #94BF27;">
         |    </div>
         |  </div>
         |  <div style="flex: 1 1 auto; line-height: 1.1; margin: 0;">
         |    $linkHtml
         |  </div>
         |</div>
         |""".stripMargin.trim

    }.mkString("\n")
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

    buffer.append(s"\\n\\n=== ${i18n.translate("Terms suggested by AI", language)} ===")
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
    translateTextEither(ollamaClient, text, textLanguage, targetLanguage).getOrElse(text)
  }

  private def translateTextEither(ollamaClient: OllamaClient,
                                  text: String,
                                  textLanguage: String,
                                  targetLanguage: String): Either[String, String] = {
    val languages: Set[String] = Set("pt", "en", "es", "fr")
    if (text.trim.isEmpty) Right(text)
    else if (!languages.contains(targetLanguage)) Left(s"Invalid target language: $targetLanguage")
    else if (!languages.contains(textLanguage)) Left(s"Invalid source language: $textLanguage")
    else if (targetLanguage.equals(textLanguage)) Right(text)
    else Try {
      val idioma: String = targetLanguage match {
        case "pt" => "portugues"
        case "es" => "espanhol"
        case "en" => "ingles"
        case _ => "frances"
      }
      val prompt: String = s"\n\nDado o seguinte texto de entrada traduza-o para o $idioma retornando como resposta unicamente o texto traduzido. Mantenha as mesmas quebras de linha do texto: $text\n\n"

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
          throw exception
      }
    }.toEither.left.map(_.getMessage)
  }

  private def detectInputLanguage(text: String): String = {
    val detector: LanguageDetector = LanguageDetectorBuilder.fromAllLanguages().build()
    val detectedLanguage: Language = detector.detectLanguageOf(text)

    detectedLanguage.name() match {
      case "ENGLISH" => "en"
      case "FRENCH" => "fr"
      case "PORTUGUESE" => "pt"
      case "SPANISH" => "es"
      case _ => "en"
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
                      isFirstLoad: Boolean,
                      translateButtonLocked: Boolean): String = {
    val escapedOriginalInputText = escapeForJsTemplateLiteral(originalInputText)
    val page: Text.TypedTag[String] = html(lang := language)(
      head(
        script(attr("async") := "", src := "https://www.googletagmanager.com/gtag/js?id=G-DBPY4Q6HT8"),
        meta(attr("charset") := "UTF-8"),
        meta(attr("http-equiv") := "Content-Type", content := "text/html; charset=UTF-8"),
        meta(attr("name") := "autor", content := " BIREME | OPAS | OMS - > Márcio Alves"),
        meta(attr("name") := "viewport", content := "width=device-width, initial-scale=1.0"),
        scalatags.Text.tags2.title("DeCSMeSH Finder - Advanced"),
        link(rel := "stylesheet", href := "decsf/css/bootstrap.min.css"),
        link(rel := "stylesheet", href := "decsf/css/fontawesome/css/all.css"),
        link(rel := "stylesheet", href := "decsf/css/bootstrap-select.css"),
        link(rel := "stylesheet", href := "decsf/css/accessibility.css?v=20260323-bars2"),
        link(rel := "stylesheet", href := "decsf/css/style.css?v=20260323-bars2"),
        link(rel := "stylesheet", href := "decsf/css/DeCSFinder.css?v=20260323-bars2"),
        link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"),
        link(rel := "shortcut icon", href := "decsf/img/favicon.png"),
        scalatags.Text.tags2.style(raw(
          """
            |.btn-success {
            |  background-color: #28a745 !important;
            |  border-color: #28a745 !important;
            |}
            |.btn-success:hover {
            |  background-color: #218838 !important;
            |  border-color: #1e7e34 !important;
            |}
            |.btn-success:focus,
            |.btn-success.focus {
            |  background-color: #218838 !important;
            |  border-color: #1e7e34 !important;
            |  box-shadow: 0 0 0 0.2rem rgba(72, 180, 97, 0.5) !important;
            |}
            |.btn-success:active,
            |.btn-success.active,
            |.show > .btn-success.dropdown-toggle {
            |  background-color: #1e7e34 !important;
            |  border-color: #1c7430 !important;
            |}
            |.btn-success:active:focus,
            |.btn-success.active:focus,
            |.show > .btn-success.dropdown-toggle:focus {
            |  box-shadow: 0 0 0 0.2rem rgba(72, 180, 97, 0.5) !important;
            |}
            |""".stripMargin
        )),
        script(src := "https://cdnjs.cloudflare.com/ajax/libs/FileSaver.js/2.0.5/FileSaver.min.js"),
        script(src := "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.4.120/pdf.min.js"),
        script(src := "decsf/js/DeCSFinder.js?v=20260327-translate-flow-2")
      ),
      body(
        cls := (if (srText.isEmpty) "no-sr-text" else "has-sr-text")
      )(
        div(id := "main_container")(
          Accessibility.accessibilityArea(language, i18n),
          Header.headerArea(originalInputText, language),
          LanguageTags.languageArea(inputLang, outLang, termTypes, language, i18n),
          MainAreas.inputOutputAreas(
            "Paste your text below",
            markedInputText.trim,
            originalInputText,
            srText,
            annifText,
            exportText,
            translateButtonLocked,
            language,
            i18n
          )
        ),
        Footer.footerX(language, i18n),
        script(src := "decsf/js/jquery-3.4.1.min.js"),
        script(src := "decsf/js/bootstrap.bundle.min.js"),
        script(src := "decsf/js/bootstrap-select.js"),
        script(src := "decsf/js/cookie.js"),
        script(src := "decsf/js/accessibility.js"),
        script(src := "decsf/js/main.js"),
        script(src := "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/js/bootstrap.bundle.min.js"),
        rawScript(
          s"""window.initializeDeCSFinderPage({
             |  originalInputText: `$escapedOriginalInputText`,
             |  language: "$language",
             |  inputLang: "$inputLang",
             |  isFirstLoad: ${isFirstLoad.toString},
             |  translateButtonLocked: ${translateButtonLocked.toString}
             |});""".stripMargin
        )
      )
    )

    "<!DOCTYPE html>\n" + page.render
  }

  private def rawScript(js: String): Text.TypedTag[String] = {
    script(raw(js))
  }

  private def replaceSubmittedBreaks(input: String, breakSignal: String): String = {
    val breakTags =
      "(?i)<br\\s*/?>|</?(?:div|section|article|header|footer|nav|aside|h1|h2|h3|h4|h5|h6|p|pre|blockquote|ul|ol|li|form|fieldset|legend|table|caption|thead|tbody|tfoot|tr|th|td|figure|figcaption|hr|main|address|canvas|video)(?:\\s+[^>]*)?>"

    input
      .replaceAll("\r?\n\r?", breakSignal)
      .replaceAll(breakTags, breakSignal)
  }

  private def normalizeSubmittedText(input: String, breakSignal: String): String = {
    val doc: Document = Jsoup.parseBodyFragment(input)
    doc.outputSettings().prettyPrint(false)

    doc.body().wholeText()
      .replace('\u00A0', ' ')
      .replace(breakSignal, "\n")
      .replaceAll("[ \\t\\x0B\\f]*\\n[ \\t\\x0B\\f]*", "\n")
      .replaceAll("\\n{3,}", "\n\n")
      .trim
  }

  private def textToHtml(value: String, breakSignal: String): String =
    value.replace(breakSignal, "<br/>")
    //value.replaceAll("\r?\n", "<br/>")

  private def escapeForJsTemplateLiteral(value: String): String = {
    value
      .replace("\\", "\\\\")
      .replace("`", "\\`")
      .replace("${", "\\${")
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
