/*=========================================================================

    DeCSMeSHFinder © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHFinder/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmf

import jakarta.servlet.{ServletConfig, ServletContext}
import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import java.io.{InputStream, PrintWriter}
import org.bireme.dh.{Config, Highlighter}

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
  * DeCSMeshHighlighter Servlet
  */
class DMFServlet extends HttpServlet {
  private var highlighter: Highlighter = _
  private var translate: Translate = _
  private var i18n: I18N = _
  private var annifBaseUrl: String = _
  private var annifProjectId: String = _

  /*
  private val yellowSquare = "\uD83D\uDFE8"  // Quadrado Amarelo
  private val redSquare = "\uD83D\uDFE5"     // Quadrado Vermelho
  private val greenSquare = "\uD83D\uDFE9"   // Quadrado Verde
  private val blueSquare = "\uD83D\uDFE6"    // Quadrado Azul
  private val orangeSquare = "\uD83D\uDFE7"  // Quadrado Laranja
  private val purpleSquare = "\uD83D\uDFEA"  // Quadrado Roxo
  */

  /**
    * Do initial web app configuration
    * @param config servlet config object
    */
  override def init(config: ServletConfig): Unit = {
    super.init(config)

    val context: ServletContext = config.getServletContext
    val decsPath:String = context.getInitParameter("DECS_PATH")
    val i18nIS: InputStream = context.getResourceAsStream("/i18n.txt")

    highlighter = new Highlighter(decsPath)
    translate = new Translate(decsPath)
    i18n = new I18N(i18nIS)
    annifBaseUrl = context.getInitParameter("ANNIF_BASE_URL")
    annifProjectId = context.getInitParameter("ANNIF_PROJECT_ID")

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
      val inputLang: Option[String] = Option(request.getParameter("inputLang")).map(_.trim)
        .flatMap(par => if (par.isEmpty) None else Some(par))
      val outLang: Option[String] = Option(request.getParameter("outLang")).map(_.trim)
        .flatMap(par => if (par.isEmpty) None else Some(par))
      val termTypes: Seq[String] = Option(request.getParameter("termTypes")).map(_.trim)
        .map(_.split(" *\\| *").toSeq).getOrElse(Seq[String]("Descriptors", "Qualifiers"))
      val inputText: String = Option(request.getParameter("inputText")).map(_.trim).getOrElse("")
      val headerLang: String = getHeaderLang(request)
      val language: String = Option(request.getParameter("lang")).map(_.trim)
        .map(l => if (l.isEmpty) headerLang else l).getOrElse(headerLang)
      val useFrequencySort: Boolean = Option(request.getParameter("frequencySort")).forall(_.toBoolean)
      val containsDescriptors: Boolean = termTypes.contains("Descriptors")
      val config = Config(
        scanLang=inputLang, outLang=outLang, scanMainHeadings=containsDescriptors, scanEntryTerms=true,
        scanQualifiers=termTypes.contains("Qualifiers"), scanPublicationTypes=containsDescriptors,
        scanCheckTags=containsDescriptors, scanGeographics=containsDescriptors
      )
      val descriptors: (String, Seq[(Int, Int, String, String, String)], Seq[(String,Int)]) =
        highlighter.highlight("«", "»", removeUnderlines(inputText), config)
        //highlighter.highlight(inputText, config)
      val annif: AnnifClient = new AnnifClient(annifBaseUrl)
      val annifSuggestions: Either[String, Seq[Suggestion]] = annif.getSuggestions(annifProjectId, inputText)
      val annifTerms: Seq[String] = getAnnifTerms(annifSuggestions, inputLang, outLang)
      val descr: Seq[String] = descriptors._3.map(_._1)
      val termsText: String = descr.mkString("\n")
      val annifText: String = annifTerms.mkString("\n")
      val exportText: String = getExportTermsText(descr, annifTerms, language)
      val outputText: String = getHtml(inputLang.getOrElse("All languages"), outLang.getOrElse("Same of the text"),
        termTypes, replaceOpenCloseTags(descriptors._1), termsText, language, annifText, exportText, useFrequencySort)
      val out: PrintWriter = response.getWriter
      out.println(outputText)
      out.flush()
    } match {
      case Success(_) => ()
      case Failure(_) => response.sendError(500, "Oops, an internal error occurred. Sorry for the inconvenience.")
    }
  }

  private def getAnnifTerms(annifSuggestions: Either[String, Seq[Suggestion]],
                            inputLang: Option[String],
                            outputLang: Option[String]): Seq[String] = {
    val annifTerms: Seq[String] = annifSuggestions.map(y => y.map(x => x.term)) match {
      case Right(terms) => inputLang match {
        case Some(iLang) =>
          val outLang: String = outputLang match {
            case Some(oLang) => if (oLang.equals("Same of the text")) iLang else oLang
            case None => iLang
          }
          if (iLang.equals("All languages")) terms
          else translate.translate(terms, iLang, outLang) match {
            case Right(translated) => translated
            case Left(_) => terms
          }
        case None => terms
      }
      case Left(error) => Seq(s"ERROR: $error")
    }
    annifTerms
  }

  private def getExportTermsText(descriptors: Seq[String],
                                 annifTerms: Seq[String],
                                 language: String): String = {
    val buffer = new StringBuilder()

    buffer.append(s"=== ${i18n.translate("Extracted descriptors", language)} ===")
    descriptors.foreach(descr => buffer.append(s"\\n$descr"))

    buffer.append(s"\\n\\n=== ${i18n.translate("Descriptors identified by AI", language)} ===")
    annifTerms.foreach(term => buffer.append(s"\\n$term"))
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

  private def replaceOpenCloseTags(in: String): String = {
    val openTag: String = "«"
    val closeTag: String = "»"

    val regex: Regex = s"$openTag(.+?)$closeTag".r

    regex.replaceAllIn(in, m => putUnderlines(m.group(1)))
  }

  private def putUnderlines(in: String): String = {
    val underlined: String = in.map(c => s"$c\u0332").mkString

    underlined
  }

  private def removeUnderlines(in: String): String = {
    // Remove o caractere de sublinhado (\u0332) da string
    in.filterNot(_ == '\u0332')
  }

  private def getHtml(inputLang: String,
                      outLang: String,
                      termTypes: Seq[String],
                      inputText: String,
                      termsText: String,
                      language: String,
                      annifText: String,
                      exportText: String,
                      useFrequencySort: Boolean): String = {
    """
<!DOCTYPE html>
<html lang="""" + language + """">
<head>
  <!-- Google tag (gtag.js) - Google Analytics -->
        <script async src="https://www.googletagmanager.com/gtag/js?id=G-DBPY4Q6HT8"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());

            gtag('config', 'G-DBPY4Q6HT8');
        </script>

	<meta charset="UTF-8">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="autor" content=" BIREME | OPAS | OMS - > Márcio Alves">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>DeCSMeSH Finder - Advanced</title>
	<link rel="stylesheet" href="decsf/css/bootstrap.min.css">
	<link rel="stylesheet" href="decsf/css/fontawesome/css/all.css">
	<link rel="stylesheet" href="decsf/css/bootstrap-select.css">
	<link rel="stylesheet" href="decsf/css/accessibility.css">
	<link rel="stylesheet" href="decsf/css/style.css">
  <link rel="shortcut icon" href="decsf/img/favicon.png">

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
</head>
<body>
	<script type="text/javascript">
		function clearTextAreas() {
			document.getElementById("inputFullText").value = "";
			document.getElementById("outputTerms").value = "";
      document.getElementById("outputAnnifTerms").value = "";
		}

		function submitPage(plang, tOrder) {
  //alert("Entrando no submitPage()");
     var inputText = document.getElementById("inputFullText").value;
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
      hiddenField6.setAttribute("name", "frequencySort");
      hiddenField6.setAttribute("value", useFreqSort);
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

    // Função para abrir a caixa de diálogo
    function abrirModal() {
        document.getElementById("modal").style.display = "flex";
    }

    // Função para fechar a caixa de diálogo
    function fecharModal() {
        document.getElementById("modal").style.display = "none";
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
					<a href="javascript:submitPageToSite('""" + language + """');"><img src="wizardDeCSF/img/logo-green-""" + language + """.svg" alt="" class="imgBlack"></a>
				</div>
				<div id="language" style="z-index: 1">
          <a href="#" onclick='submitPage("en", "");'>English</a>
          <a href="#" onclick='submitPage("es", "");'>Español</a>
          <a href="#" onclick='submitPage("pt", "");'>Português</a>
          <a href="#" onclick='submitPage("fr", "");'>Français</a>
				</div>
				<!--div class="col-12">
					<a href="javascript:submitPageToSite('""" + language + """');"><img src="wizardDeCSF/img/logo-green-""" + language + """.svg" alt="" class="imgBlack"></a>
				</div-->
			</div>
		</div>
	</header>

	<section id="filter">
		<div class="container">
			<div class="row">
				<div class="form-group col-md-4">
					<label for="">""" + i18n.translate("Language of your text", language) + """:</label>
					<select name="" id="inputTextLanguage" class="form-control">
						<option value="" """ + (if (inputLang.equals("All languages")) "selected=\"\"" else "") + """>""" + i18n.translate("I don't know", language) + """</option>
						<option value="en" """ + (if (inputLang.equals("en")) "selected=\"\"" else "") + """>""" + i18n.translate("English", language) + """</option>
						<option value="es" """ + (if (inputLang.equals("es")) "selected=\"\"" else "") + """>""" + i18n.translate("Spanish", language) + """</option>
						<option value="pt" """ + (if (inputLang.equals("pt")) "selected=\"\"" else "") + """>""" + i18n.translate("Portuguese", language) + """</option>
						<option value="fr" """ + (if (inputLang.equals("fr")) "selected=\"\"" else "") + """>""" + i18n.translate("French", language) + """</option>
					</select>
				</div>
				<div class="form-group col-md-4">
					<label for="">""" + i18n.translate("Language of the terms", language) + """:</label>
					<select name="" id="outputTextLanguage" class="form-control">
            <option value="" """ + (if (outLang.equals("Same of the text")) "selected=\"\"" else "") + """>""" + i18n.translate("The same found in the text", language) + """</option>
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
          <label for="">""" +
             i18n.translate("Paste your text below", language) + """:
          </label>
          <div style="display: flex; align-items: flex-start;">
            <textarea name="" id="inputFullText" cols="30" rows="7" class="form-control">""" + inputText + """</textarea>
            <div class="btn-group" role="group" aria-label="Basic example" style="display: flex; flex-direction: column; justify-content: flex-start; margin-left: 10px;">
              <!--button type="button" class="btn btn-success" title='""" + i18n.translate("Search", language) + """' onclick='submitPage("", "")'-->
              <button type="button" class="btn btn-success" title='""" + i18n.translate("Search", language) + """'
                       onclick='submitPage("", ""); gtag("event", "button_click", {
                           "event_category": "button", "event_label": "Search Button"
                           });'>
                <i class="fas fa-search"></i>
              </button>
              <!--button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Clear", language) + """' onclick="clearTextAreas()"-->
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
        <div class="form-group col-md-6">
          <label for="">
              """ + i18n.translate("Extracted descriptors", language) + """:
          </label>
          <div style="display: flex; align-items: flex-start;">
            <textarea name="" id="outputTerms" cols="30" rows="11" class="form-control">""" + termsText + """
            </textarea>
          </div>
        </div>

        <!-- Nova textarea "Additional Notes" -->
        <div class="form-group col-md-6">
          <label for="">
            """ + i18n.translate("Terms identified by AI", language) + """:
          </label>
          <div style="display: flex; align-items: flex-start;">
            <textarea name="" id="outputAnnifTerms" cols="30" rows="11" class="form-control">""" + annifText + """</textarea>
            <div class="btn-group" role="group" aria-label="Basic example" style="display: flex; flex-direction: column; justify-content: flex-start; margin-left: 10px;">
            <!--button type="button" class="btn btn-success" title='""" + i18n.translate("Alphabetical order", language) + """' onclick='submitPage("", "false")'>
              <i class="fas fa-sort-alpha-down"></i>
            </button>
            <button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Numerical order", language) + """' onclick='submitPage("", "true")'>
              <i class="fas fa-sort-numeric-down-alt"></i>
            </button-->
            <!--button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Export to file", language) + """' onclick='exportTerms()'-->
            <button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Export to file", language) + """'
                onclick='exportTerms(); gtag("event", "button_click", {
                "event_category": "button", "event_label": "Export Button"
              });'>
              <i class="fas fa-file-export"></i>
            </button>
            <!--button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Send your comments", language) + """' onclick="window.open('https://contacto.bvsalud.org/chat.php?group=DeCSMeSH%20Finder&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__', '_blank');"-->
            <button type="button" class="btn btn-success" style="margin-top: 1px;" title='""" + i18n.translate("Send your comments", language) + """'
                onclick='window.open("https://contacto.bvsalud.org/chat.php?group=DeCSMeSH%20Finder&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__", "_blank"); gtag("event", "button_click", {
                "event_category": "button", "event_label": "Comments Button"
              });'>
              <i class="fas fa-comment"></i>
            </button>
            <!--button type="button" class="btn btn-success" style="background-color: red; margin-top: 1px;" title='""" + i18n.translate("Warning", language) + """' onclick='abrirModal()'-->
            <button type="button" class="btn btn-success" style="background-color: red; margin-top: 1px;" title='""" + i18n.translate("Warning", language) + """'
                onclick='abrirModal(); gtag("event", "button_click", {
                "event_category": "button", "event_label": "Warning Button"
              });'>
              <i class="fas fa-exclamation-triangle"></i>
            </button>
          </div>
        </div>

        <div id="modal">
          <div id="modal-content">
            <p><strong><i class="fas fa-exclamation-triangle"></i></strong>  """ + i18n.translate("Notice", language) + """</p>
            <button id="close-btn" onclick="fecharModal()">OK</button>
          </div>
        </div>

      </div>
		</div>
	</main>

  <!--div class="container">
		<div class="alert alert-warning alert-dismissible fade show" role="alert">
			<div id="disclaimer">
				<p><strong><i class="fas fa-exclamation-triangle"></i></strong>  """ + i18n.translate("Notice", language) + """</p>
			</div>
			<button type="button" class="btDisclaimer">
				<span class="acordionIcone fas fa-angle-down" style="font-size: 25px;"></span>
			</button>
			<div class="disclaimerTransparente"></div>
		</div>
	</div-->

  <footer id="footer" class="padding1">
      <div class="container">
          <div class="row">
              <div class="col-md-5">
                  <b>DeCS/MeSH Finder</b> <br>
                  <a href="http://politicas.bireme.org/terminos/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Terms and conditions of use", language) + """</a>
                  <a href="http://politicas.bireme.org/privacidad/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Privacy policy", language) + """</a>
              </div>
              <div class="col-md-7 text-right">""" +
                  (language match {
                    case "es" => "<a href=\"https://www.bireme.org/es/home-espanol/\" target=\"_blank\">"
                    case "pt" => "<a href=\"https://www.bireme.org/\" target=\"_blank\">"
                    case _ => "<a href=\"https://www.bireme.org/en/home-english/\" target=\"_blank\">"
                  }) + """
                  <img src="http://logos.bireme.org/img/""" + language + """/h_bir_white.svg" alt="" class="img-fluid">
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
      });
  </script>

  <script src="decsf/js/jquery-3.4.1.min.js"></script>
	<script src="decsf/js/bootstrap.bundle.min.js"></script>
	<script src="decsf/js/bootstrap-select.js"></script>
	<script src="decsf/js/cookie.js"></script>
	<script src="decsf/js/accessibility.js"></script>
	<script src="decsf/js/main.js"></script>
</body>
</html>
    """
  }
}
