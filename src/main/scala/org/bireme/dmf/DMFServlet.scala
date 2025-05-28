/*=========================================================================

    DeCSMeSHFinder © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHFinder/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmf

import com.github.pemistahl.lingua.api.{Language, LanguageDetector, LanguageDetectorBuilder}
import jakarta.servlet.{ServletConfig, ServletContext}
import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

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

    Try {
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
      val inputText: String = doc.body().text().trim

      require(inputText.length < 200000, s"Your text size is greater than 200,000 characters. Size=[${inputText.length}]")
      //println(s"inputText=$inputText")
      val headerLang: String = getHeaderLang(request)
      val language: String = Option(request.getParameter("lang")).map(_.trim)
        .map(l => if (l.isEmpty) headerLang else l).getOrElse(headerLang)
      val useFrequencySort: Boolean = Option(request.getParameter("frequencySort")).forall(_.toBoolean)

      val outputText: String = if (inputText.isEmpty) {
        getHtml(inputLang="", outLang=language /*"Same of the text"*/, termTypes, markedInputText="", inputText, language,
          annifText="", exportText="", useFrequencySort=useFrequencySort, isFirstLoad=isFirstLoad)
      } else {
        val inputLang: String = Option(request.getParameter("inputLang")).map(_.trim).getOrElse("All languages") match {
          case "" => "All languages"
          case "All languages" =>
            val detector: LanguageDetector = LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.FRENCH, Language.PORTUGUESE, Language.SPANISH).build()
            val detectedLanguage: Language = detector.detectLanguageOf(inputText)
            println(s"detectedLanguage=${detectedLanguage.name()}")
            val lang: String = detectedLanguage.name() match {
              case "ENGLISH" => "en"
              case "FRENCH" => "fr"
              case "PORTUGUESE" => "pt"
              case "SPANISH" => "es"
              case _ => "en"
            }
            //println(s"lang=$lang")
            lang
          case "en" => "en"
          case "fr" => "fr"
          case "pt" => "pt"
          case "es" => "es"
          case _ => "en"
        }
        //println(s"inputLang=$inputLang")

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
        //highlighter.highlight("[", "]",  inputText, config)
        //println(s"descriptors=${descriptors._1}")
        val annif: AnnifClient = new AnnifClient(annifBaseUrl)
        val annifSuggestions: Either[String, Seq[AnnifSuggestion]] = inputLang match {
          case "pt" => annif.getSuggestions(annifProjectId_pt, inputText, limit = Some(15))
          case "es" => annif.getSuggestions(annifProjectId_es, inputText, limit = Some(15))
          case "en" => annif.getSuggestions(annifProjectId_en, inputText, limit = Some(15))
          case _ => Right(Seq[AnnifSuggestion]())
        }
        //val annifSuggestions: Either[String, Seq[Suggestion]] = Right(Seq[Suggestion]())
        val annifTerms: Seq[(String, Option[String])] = getAnnifTerms(annifSuggestions, Some(inputLang), outLang).getOrElse(Seq[(String, Option[String])]())
        val annifTermsPrefSuf: Seq[String] = annifTerms.map(term => markPrefSuffix.prefSuffix1(term._1, termLang = oLanguage, tipLang = language))
        //val descr: Seq[String] = descriptors._3.map(_._1)
        val descr: Seq[(String,String)] = descriptors._2.map(t => (t._3, t._5))
        val exportText: String = getExportTermsText(descr, annifTerms, language)

        //inputText.foreach(ch => println(s"[$ch]=${ch.toInt}"))
        //println(s"Antes de chamar o getHtml.\ninputText=[$inputText]\ninputText.replace=[${inputText.replace("\n", "<br>")}]")
        getHtml(inputLang, oLanguage,
          //termTypes, descriptors._1, inputText, language, "aqui termos Annif", exportText, useFrequencySort, isFirstLoad)
          termTypes, descriptors._1.replace(breakSignal, "<br/>"), inputText.replace("\n", "<br>"), language, annifTermsPrefSuf.mkString("<br>"), exportText, useFrequencySort, isFirstLoad)
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
        val errMess: String = exception match {
          case _: java.lang.StackOverflowError => s"Oops, it seems that your text is too long!"
          case _ => s"Oops, an internal error occurred. Sorry for the inconvenience.\n\n${exception.getMessage}!"
        }

        response.sendError(500, errMess)
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

  private def getExportTermsText(descriptors: Seq[(String, String)],
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
    val header = Option(request.getHeader("Accept-Language")).map(_.toLowerCase).getOrElse("pt")
    val langs: Array[String] = header.split(",|;")

    langs.find {
      lang => lang.equals("en") || lang.equals("es") || lang.equals("pt") || lang.equals("fr")
    }.getOrElse("pt")
  }

  private def getHtml(inputLang: String,          // Language of the text entered by user
                      outLang: String,            // Language of the terms to be used in the found terms
                      termTypes: Seq[String],     // Descriptors and/or Qualifiers
                      markedInputText: String,    // Text entered by the user and with its terms marked with tooltips
                      originalInputText: String,  // Text entered by the user
                      language: String,           // Language used in the interface
                      annifText: String,          // Terms identified by Annif and marked with tooltip
                      exportText: String,         // Text put in the export file
                      useFrequencySort: Boolean,
                      isFirstLoad: Boolean): String = {

    //println(s"markedInputText=[${Option(markedInputText).getOrElse("<vazio>")}]")
    //println(s"originalInputText=[${Option(originalInputText).getOrElse("<vazio>")}]")

    val warningMessage: String = language match {
      case "en" => "Find ideal descriptors with DeCS Finder IA.\nSimplify the indexing of your texts with artificial intelligence. DeCS Finder IA, developed by BIREME, identifies DeCS/MeSH descriptors for your content quickly and precisely.\nTry it now and boost your search!"
      case "es" => "Encuentre los descriptores ideales con DeCS Finder IA.\nSimplifique la indización de sus textos con inteligencia artificial. DeCS Finder IA, desarrollado por BIREME, identifica descriptores DeCS/MeSH para su contenido de forma rápida y precisa.\n¡Pruébelo ahora y potencie su búsqueda!"
      case "pt" => "Encontre descritores ideais com o DeCS Finder IA.\nSimplifique a indexação dos seus textos com inteligência artificial. O DeCS Finder IA, desenvolvido pela BIREME, identifica os descritores DeCS/MeSH para o seu conteúdo, de forma rápida e precisa.\nExperimente agora e potencialize sua pesquisa!"
      case "fr" => "Trouvez les descripteurs idéaux avec DeCS Finder IA.\nSimplifiez l'indexation de vos textes grâce à l'intelligence artificielle. DeCS Finder IA, développé par BIREME, identifie rapidement et précisément les descripteurs DeCS/MeSH de vos contenus.\nEssayez-le maintenant et boostez votre recherche!"
      case _ => "Find ideal descriptors with DeCS Finder IA.\nSimplify the indexing of your texts with artificial intelligence. DeCS Finder IA, developed by BIREME, identifies DeCS/MeSH descriptors for your content quickly and precisely.\nTry it now and boost your search!"
    }

    s"""
<!DOCTYPE html>
<html lang="""" + language + """">

<head>
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
        height: 200px;
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
        height: 200px;
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

</head>

<body>
	<script type="text/javascript">
		function clearTextAreas() {
      //alert("entrando no clearTextAreas");
      var textWithTooltips = document.getElementById("textWithTooltips");
      var textWithTooltipsAnnif = document.getElementById("textWithTooltipsAnnif");
			textWithTooltips.textContent = "";
      textWithTooltips.setAttribute("contenteditable", "true");
      textWithTooltipsAnnif.textContent = "";
      textWithTooltipsAnnif.setAttribute("contenteditable", "true");
      submitPage("", "");
		}

		function submitPage(plang, tOrder) {
     //alert("Entrando no submitPage()");
     //alert("originalInputText=[""" + originalInputText + """]");
     var inputText0 = document.getElementById('textWithTooltips').innerHTML;
     //alert("inputText0=[" + inputText0 + "]");

     var inputText= "";
     if (inputText0.includes("tooltip-link")) {
      inputText = `""" + originalInputText.replace("`", "'") + """`;
     } else {
       inputText = inputText0;
     }
     //alert("inputText=[" + inputText + "]");

     var inputLang = document.getElementById("inputTextLanguage").value;
     var outputLang = document.getElementById("outputTextLanguage").value;

     //alert("plang=[" + plang + "]");
     var pageLang = """" + language + """";
     var language;
     if (plang === "") language = pageLang;
     else language = plang;

      var useFreqSort;
      if (tOrder === "") useFreqSort = """ + useFrequencySort.toString + """; else useFreqSort = tOrder;

			var termTypes = new Array();
      var trTypes = document.getElementById("termTypes");
      var i;
      var count = 0;

      for (i = 0; i < trTypes.options.length; i++) {
        if (trTypes.options[i].selected) {
          termTypes[count] = trTypes.options[i].value;
          count = count + 1
        }
      }

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", "dmf");
      form.acceptCharset = "UTF-8";

      var hiddenField1 = document.createElement("input");
      hiddenField1.setAttribute("type", "hidden");
      hiddenField1.setAttribute("name", "inputLang");
      hiddenField1.setAttribute("value", inputLang);
      form.appendChild(hiddenField1);

      var hiddenField2 = document.createElement("input");
      hiddenField2.setAttribute("type", "hidden");
      hiddenField2.setAttribute("name", "outLang");
      hiddenField2.setAttribute("value", outputLang);
      form.appendChild(hiddenField2);

      var hiddenField3 = document.createElement("input");
      hiddenField3.setAttribute("type", "hidden");
      hiddenField3.setAttribute("name", "inputText");
      hiddenField3.setAttribute("value", inputText);
      form.appendChild(hiddenField3);

      var termTypesStr = "";
			for (i = 0; i < termTypes.length; i++) {
        if (i > 0) {
          termTypesStr = termTypesStr + "|";
        }
        termTypesStr = termTypesStr + termTypes[i];
      }

      var hiddenField4 = document.createElement("input");
      hiddenField4.setAttribute("type", "hidden");
      hiddenField4.setAttribute("name", "termTypes");
      hiddenField4.setAttribute("value", termTypesStr);
      form.appendChild(hiddenField4);

      var hiddenField5 = document.createElement("input");
      hiddenField5.setAttribute("type", "hidden");
      hiddenField5.setAttribute("name", "lang");
      hiddenField5.setAttribute("value", language);
      form.appendChild(hiddenField5);

      var hiddenField6 = document.createElement("input");
      hiddenField6.setAttribute("type", "hidden");
      hiddenField6.setAttribute("name", "isFirstLoad");
      hiddenField6.setAttribute("value", "false");
      form.appendChild(hiddenField6);

      document.body.appendChild(form);

      form.submit();
		}

    function submitPageToSite(plang) {
        var pageLang = """" + language + """";
        var language;
        if (plang === "") language = pageLang;
        else language = plang;

        var formS = document.createElement("form");
        formS.setAttribute("method", "post");
        formS.setAttribute("action", "dmfs");

        var hiddenFieldLang = document.createElement("input");
        hiddenFieldLang.setAttribute("type", "hidden");
        hiddenFieldLang.setAttribute("name", "lang");
        hiddenFieldLang.setAttribute("value", language);
        formS.appendChild(hiddenFieldLang);

        document.body.appendChild(formS);

        formS.submit();
    }

    function exportTerms() {
        //alert("entrando no exportTerms");

        let expoText = """" + exportText + """";

        if (expoText != null && expoText.trim() !== "")  {
          const now = new Date();
          let hours = now.getHours().toString().padStart(2, '0');
          let minutes = now.getMinutes().toString().padStart(2, '0');
          let seconds = now.getSeconds().toString().padStart(2, '0');
          let fileName = `DeCSFinder_${hours}:${minutes}:${seconds}`;
          let blob = new Blob([expoText], { type: "text/plain;charset=utf-8" });
          saveAs(blob, fileName + ".txt");
        }
    }
	</script>

	<section id="barAccessibility">
		<div class="container">
			<div class="row">
				<div class="col-md-6" id="accessibilityTutorial">
					<a href="#main_container" tabindex="1" role="button">""" + i18n.translate("Main Content", language) + """ <span class="hiddenMobile">1</span></a>
          <!-- <a href="#nav" tabindex="2" role="button">Menu <span class="hiddenMobile">2</span></a>
				  <a href="#fieldSearch" tabindex="3" id="accessibilitySearch" role="button">Busca <span class="hiddenMobile">3</span></a> -->
					<a href="#footer" tabindex="4" role="button">""" + i18n.translate("Footer", language) + """ <span class="hiddenMobile">4</span></a>
				</div>
				<div class="col-md-6" id="accessibilityFontes">
					<a href="#!" id="fontPlus"  tabindex="5" aria-hidden="true">+A</a>
					<a href="#!" id="fontNormal"  tabindex="6" aria-hidden="true">A</a>
					<a href="#!" id="fontLess"  tabindex="7" aria-hidden="true">-A</a>
					<a href="#!" id="contraste"  tabindex="8" aria-hidden="true"><i class="fas fa-adjust"></i> """ + i18n.translate("High Contrast", language) + """</a>
					<a href="https://politicas.bireme.org/accesibilidad/""" + (if (language.equals("fr")) "en" else language) + """" role="button" id="accebilidade" tabindex="9" target="_blank" title='""" + i18n.translate("Accessibility", language) + """'><i class="fas fa-wheelchair"></i></a>
				</div>
			</div>
		</div>
	</section>

	<header id="header">
		<div class="container">
			<div class="row" style="position: relative;">
        <div class="col-12">
					<a href="javascript:submitPageToSite('""" + language + """');"><img src="decsf/img/decs-finder-color-""" + language + """.svg" alt="" class="imgBlack"/></a>
				</div>
				<div id="language" style="z-index: 1">
          <a href="#" onclick='submitPage("en", "");'>English</a>
          <a href="#" onclick='submitPage("es", "");'>Español</a>
          <a href="#" onclick='submitPage("pt", "");'>Português</a>
          <a href="#" onclick='submitPage("fr", "");'>Français</a>
				</div>
			</div>
		</div>
	</header>

	<section id="filter">
		<div class="container">
			<div class="row">
				<div class="form-group col-md-4">
					<label for="">""" + i18n.translate("Language of your text", language) + """:</label>
					<select name="" id="inputTextLanguage" class="form-control">
						<option value="All languages" """ + (if (inputLang.equals("All languages")) "selected=\"\"" else "") + """>""" + i18n.translate("I don't know", language) + """</option>
						<option value="en" """ + (if (inputLang.equals("en")) "selected=\"\"" else "") + """>""" + i18n.translate("English", language) + """</option>
						<option value="es" """ + (if (inputLang.equals("es")) "selected=\"\"" else "") + """>""" + i18n.translate("Spanish", language) + """</option>
						<option value="pt" """ + (if (inputLang.equals("pt")) "selected=\"\"" else "") + """>""" + i18n.translate("Portuguese", language) + """</option>
						<option value="fr" """ + (if (inputLang.equals("fr")) "selected=\"\"" else "") + """>""" + i18n.translate("French", language) + """</option>
					</select>
				</div>
				<div class="form-group col-md-4">
					<label for="">""" + i18n.translate("Language of the terms", language) + """:</label>
					<select name="" id="outputTextLanguage" class="form-control">
            <!-- option value="Same of the text" """ + (if (outLang.equals("Same of the text")) "selected=\"\"" else "") + """>""" + i18n.translate("The same found in the text", language) + """</option-->
						<option value="en" """ + (if (outLang.equals("en")) "selected=\"\"" else "") + """>""" + i18n.translate("English", language) + """</option>
						<option value="es" """ + (if (outLang.equals("es")) "selected=\"\"" else "") + """>""" + i18n.translate("Spanish", language) + """</option>
						<option value="pt" """ + (if (outLang.equals("pt")) "selected=\"\"" else "") + """>""" + i18n.translate("Portuguese", language) + """</option>
						<option value="fr" """ + (if (outLang.equals("fr")) "selected=\"\"" else "") + """>""" + i18n.translate("French", language) + """</option>
					</select>
				</div>
				<div class="form-group col-md-4">
					<label for="">""" + i18n.translate("Types of terms", language) + """:</label>
					<select multiple class="selectpicker form-control" id="termTypes" placeholder="">
						<option value="Descriptors"""" + (if (termTypes.contains("Descriptors")) " selected=\"\"" else "") + """>""" + i18n.translate("Descriptors", language) + """</option>
						<option value="Qualifiers"""" + (if (termTypes.contains("Qualifiers")) " selected=\"\"" else "") + """>""" + i18n.translate("Qualifiers", language) + """</option>
					</select>
				</div>
			</div>
		</div>
	</section>

	<main id="main_container" class="padding1">
    <div class="container">
        <div class="row">
            <div class="form-group col-md-12">
                <label>""" + i18n.translate("Paste your text below", language) + """:</label>
                <div style="display: flex; align-items: flex-start;">
                    <div id="textWithTooltips" class="p-3 border rounded" spellcheck="false" contenteditable="""" + (if (markedInputText.trim.isEmpty) "true" else "false")+ """">""" + markedInputText + """</div>
                    <div class="btn-group" role="group" aria-label="Basic example" style="display: flex; flex-direction: column; justify-content: flex-start; margin-left: 10px;">
                        <button type="button" class="btn btn-success" title='""" + i18n.translate("Search", language) + """'
                            onclick='submitPage("", ""); gtag("event", "button_click", {
                                "event_category": "button", "event_label": "Search Button"
                            });'>
                            <i class="fas fa-search"></i>
                        </button>
                        <button type="button" class="btn btn-success" style="margin-top: 1px;"  title='""" + i18n.translate("Clear", language) + """'
                       onclick='clearTextAreas(); gtag("event", "button_click", {
                           "event_category": "button", "event_label": "Clear Button"
                           });'>
                        <i class="far fa-trash-alt"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
          <!-- Display Annif terms -->
          <div class="form-group col-md-12">
            <label>""" + i18n.translate("Terms identified by AI", language) + """:</label>
            <div style="display: flex; align-items: flex-start;">
              <div id="textWithTooltipsAnnif" class="p-3 border rounded" style="flex-grow: 1;" spellcheck="false" contenteditable="false">""" + annifText + """</div>
              <div class="btn-group" role="group" aria-label="Basic example" style="display: flex; flex-direction: column; justify-content: flex-start; margin-left: 10px;">
              <button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Export to file", language) + """'
                  onclick='exportTerms(); gtag("event", "button_click", {
                  "event_category": "button", "event_label": "Export Button"
                });'>
                  <i class="fas fa-file-export"></i>
              </button>
              <button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Send your comments", language) + """'
                  onclick='window.open("https://contacto.bvsalud.org/chat.php?group=DeCSMeSH%20Finder&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__", "_blank"); gtag("event", "button_click", {
                  "event_category": "button", "event_label": "Comments Button"
                });'>
                <i class="fas fa-comment"></i>
              </button>
            </div>
          </div>
        </div>
    </div>
  </main>

  <!-- div class="container">
		<div class="alert alert-warning alert-dismissible fade show" role="alert">
			<div id="disclaimer">
				<p><strong><i class="fas fa-exclamation-triangle"></i></strong>  """ + i18n.translate("Notice", language) + """</p>
			</div>
			<button type="button" class="btDisclaimer">
				<span class="acordionIcone fas fa-angle-down" style="font-size: 25px;"></span>
			</button>
			<div class="disclaimerTransparente"></div>
		</div>
	</div  -->

  <footer id="footer" class="padding1">
      <div class="container">
          <div class="row">
              <div class="col-md-5">
                  <b>DeCS Finder IA</b> <br/>
                  <a href="http://politicas.bireme.org/terminos/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Terms and conditions of use", language) + """</a>
                  <a href="http://politicas.bireme.org/privacidad/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Privacy policy", language) + """</a>
              </div>
              <div class="col-md-7 text-right">""" +
      (language match {
        case "es" => "<a href=\"https://www.bireme.org/es/home-espanol/\" target=\"_blank\">"
        case "pt" => "<a href=\"https://www.bireme.org/\" target=\"_blank\">"
        case _ => "<a href=\"https://www.bireme.org/en/home-english/\" target=\"_blank\">"
      }) + """
                  <img src="http://logos.bireme.org/img/""" + language + """/h_bir_white.svg" alt="" class="img-fluid" />
                </a>
              </div>
          </div>
      </div>
  </footer>

  <script>
          // Captura o evento de mudança no select
        document.getElementById('inputTextLanguage').addEventListener('change', function() {
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
            submitPage("", "")

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
            submitPage("", "")
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
          submitPage("", "");
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

      el.addEventListener('paste', (e) => {
        // extrai apenas o texto colado
        const pastedText = e.clipboardData.getData('text');
        onPaste(el, pastedText);
      });

      function onPaste(el, text) {
        //alert('Texto colado:' + text);
        // coloque aqui sua lógica
        el.innerHTML = text;
        submitPage("", "");
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
}
