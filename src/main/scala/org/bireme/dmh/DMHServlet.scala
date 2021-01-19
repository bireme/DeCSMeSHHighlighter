/*=========================================================================

    DeCSMeSHHighlighter © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHHighlighter/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmh

import java.io.{InputStream, PrintWriter}

import javax.servlet.{ServletConfig, ServletContext}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.bireme.dh.{Config, Highlighter}

import scala.util.{Failure, Success, Try}

/**
  * DeCSMeshHigligther Servlet
  */
class DMHServlet extends HttpServlet {
  var highlighter: Highlighter = _
  var i18n: I18N = _

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
    i18n = new I18N(i18nIS)

    println("DMHServlet is listening ...")
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

    Try {
      val inputLang: Option[String] = Option(request.getParameter("inputLang")).map(_.trim)
        .flatMap(par => if (par.isEmpty) None else Some(par))
      val outLang: Option[String] = Option(request.getParameter("outLang")).map(_.trim)
        .flatMap(par => if (par.isEmpty) None else Some(par))
//println(s"==>inputLang=$inputLang outLang=$outLang")
//println(s"termTypesStr=${request.getParameter("termTypes")}")
      val termTypes: Seq[String] = Option(request.getParameter("termTypes")).map(_.trim)
        .map(_.split(" *\\| *").toSeq).getOrElse(Seq[String]("Main headings"))
//println(s"==>termTypes=$termTypes")
      val inputText: String = Option(request.getParameter("inputText")).map(_.trim)
        .map(_.replaceAll("[«»]", "")).getOrElse("")
      val headerLang: String = getHeaderLang(request)
      val language: String = Option(request.getParameter("lang")).map(_.trim)
        .map(l => if (l.isEmpty) headerLang else l).getOrElse(headerLang)
      val config = Config(
        scanLang = inputLang, outLang, termTypes.contains("Main headings"), termTypes.contains("Entry terms"),
        termTypes.contains("Qualifiers"), termTypes.contains("Publication types"), termTypes.contains("Check tags"),
        termTypes.contains("Geographics")
      )
      val descriptors: (String, Seq[(Int, Int, String, String, String)], Seq[String]) =
        highlighter.highlight("«", "»", inputText, config)
      val outputText: String = getHtml(inputLang.getOrElse("All languages"), outLang.getOrElse("Same of the text"),
        termTypes, descriptors._1, descriptors._3, language)
      val out: PrintWriter = response.getWriter
      out.println(outputText)
      out.flush()
    } match {
      case Success(_) => ()
      case Failure(_) => response.sendError(500, "Oops, an internal error occurred. Sorry for the inconvenience.")
    }
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

  private def getHtml(inputLang: String,
                      outLang: String,
                      termTypes: Seq[String],
                      inputText: String,
                      descriptors: Seq[String],
                      language: String): String = {
    """
<!DOCTYPE html>
<html lang="""" + language + """">
<head>
  <!-- Global site tag (gtag.js) - Google Analytics -->
  <script async src="https://www.googletagmanager.com/gtag/js?id=UA-39600115-37"></script>
  <script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());

    gtag('config', 'UA-39600115-37');
  </script>

	<meta charset="UTF-8">
  <meta name="autor" content=" BIREME | OPAS | OMS - > Márcio Alves">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>DeCSMeSH Highlighter - Advanced</title>
	<link rel="stylesheet" href="decsh/css/bootstrap.min.css">
	<link rel="stylesheet" href="decsh/css/fontawesome/css/all.css">
	<link rel="stylesheet" href="decsh/css/bootstrap-select.css">
	<link rel="stylesheet" href="decsh/css/accessibility.css">
	<link rel="stylesheet" href="decsh/css/style.css">
  <link rel="shortcut icon" href="decsh/img/favicon.png">
</head>
<body>
	<script type="text/javascript">
		function clearTextAreas() {
			document.getElementById("inputFullText").value = "";
			document.getElementById("outputTerms").value = "";
		}

		function submitPage(plang) {
//alert("Entrando no submitPage()");
			var inputText = document.getElementById("inputFullText").value;
			var inputLang = document.getElementById("inputTextLanguage").value;
			var outputLang = document.getElementById("outputTextLanguage").value;

//alert("plang=[" + plang + "]");
	    var pageLang = """" + language + """";
      var language;
      if (plang === "") language = pageLang;
      else language = plang;

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
      form.setAttribute("action", "dmh");
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
        formS.setAttribute("action", "dmhs");

        var hiddenFieldLang = document.createElement("input");
        hiddenFieldLang.setAttribute("type", "hidden");
        hiddenFieldLang.setAttribute("name", "lang");
        hiddenFieldLang.setAttribute("value", language);
        formS.appendChild(hiddenFieldLang);

        document.body.appendChild(formS);

        formS.submit();
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
				<div id="language" style="z-index: 1">
          <a href="#" onclick='submitPage("en");'>English</a>
          <a href="#" onclick='submitPage("es");'>Español</a>
          <a href="#" onclick='submitPage("pt");'>Português</a>
          <a href="#" onclick='submitPage("fr");'>Français</a>
				</div>
				<div class="col-12">
					<a href="javascript:submitPageToSite('""" + language + """');"><img src="wizardDeCSH/img/logo-green-""" + language + """.svg" alt="" class="imgBlack"></a>
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
						<option value="" """ + (if (inputLang.equals("All languages")) "selected=\"\"" else "") + """>""" + i18n.translate("I don't know", language) + """</option>
						<option value="en" """ + (if (inputLang.equals("en")) "selected=\"\"" else "") + """>""" + i18n.translate("English", language) + """</option>
						<option value="es" """ + (if (inputLang.equals("es")) "selected=\"\"" else "") + """>""" + i18n.translate("Spanish", language) + """</option>
						<option value="pt" """ + (if (inputLang.equals("pt")) "selected=\"\"" else "") + """>""" + i18n.translate("Portuguese", language) + """</option>
						<option value="fr" """ + (if (inputLang.equals("fr")) "selected=\"\"" else "") + """>""" + i18n.translate("French", language) + """</option>
					</select>
				</div>
				<div class="form-group col-md-4">
					<label for="">""" + i18n.translate("Language of localized terms", language) + """:</label>
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
						<option value="Main headings"""" + (if (termTypes.contains("Main headings")) " selected=\"\"" else "") + """>""" + i18n.translate("Main headings", language) + """</option>
						<option value="Qualifiers"""" + (if (termTypes.contains("Qualifiers")) " selected=\"\"" else "") + """>""" + i18n.translate("Qualifiers", language) + """</option>
						<option value="Entry terms"""" + (if (termTypes.contains("Entry terms")) " selected=\"\"" else "") + """>""" + i18n.translate("Entry terms", language) + """</option>
						<option value="Publication types"""" + (if (termTypes.contains("Publication types")) " selected=\"\"" else "") + """>""" + i18n.translate("Publication types", language) + """</option>
						<option value="Check tags"""" + (if (termTypes.contains("Check tags")) " selected=\"\"" else "") + """>""" + i18n.translate("Check tags", language) + """</option>
						<option value="Geographics"""" + (if (termTypes.contains("Geographics")) " selected=\"\"" else "") + """>""" + i18n.translate("Geographics", language) + """</option>
					</select>
				</div>
			</div>

		</div>
	</section>

	<main id="main_container" class="padding1">
		<div class="container">
			<div class="row">
				<div class="col-md-8">
					<div class="form-group">
						<label for="">""" + i18n.translate("Paste your text below", language) + """</label>
						<textarea name="" id="inputFullText" cols="30" rows="10" class="form-control">""" + inputText + """</textarea>
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="">""" + i18n.translate("Terms found", language) + """</label>
						<textarea name="" id="outputTerms" cols="30" rows="10" class="form-control">""" + descriptors.mkString("\n") + """</textarea>
					</div>
				</div>

				<div class="col-md-12">
					<div class="btn-group" role="group" aria-label="Basic example">
						<!-- <button type="button" class="btn btn-success" title="Setting"><i class="fas fa-sliders-h"></i></button> -->
						<button type="button" class="btn btn-success" title='""" + i18n.translate("Clear", language) + """' onclick="clearTextAreas()"><i class="far fa-trash-alt"></i></button>
						<button type="button" class="btn btn-success" title='""" + i18n.translate("Search", language) + """' onclick='submitPage("")'><i class="fas fa-search"></i></button>
					</div>
				</div>
			</div>
		</div>
	</main>

  <div class="container">
		<div class="alert alert-warning alert-dismissible fade show" role="alert">
			<div id="disclaimer">
				<p><strong><i class="fas fa-exclamation-triangle"></i></strong>  """ + i18n.translate("Notice", language) + """</p>
			</div>
			<button type="button" class="btDisclaimer">
				<span class="acordionIcone fas fa-angle-down" style="font-size: 25px;"></span>
			</button>
			<div class="disclaimerTransparente"></div>
		</div>
	</div>

  <footer id="footer" class="padding1">
      <div class="container">
          <div class="row">
              <div class="col-md-5">
                  <b>DeCS/MeSH Highlighter</b> <br>
                  <a href="http://politicas.bireme.org/terminos/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Terms and conditions of use", language) + """</a>
                  <a href="http://politicas.bireme.org/privacidad/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Privacy policy", language) + """</a>
              </div>
              <div class="col-md-7 text-right">
                  <img src="http://logos.bireme.org/img/""" + language + """/h_bir_white.svg" alt="" class="img-fluid">
              </div>
          </div>
      </div>
  </footer>

  <script src="decsh/js/jquery-3.4.1.min.js"></script>
	<script src="decsh/js/bootstrap.bundle.min.js"></script>
	<script src="decsh/js/bootstrap-select.js"></script>
	<script src="decsh/js/cookie.js"></script>
	<script src="decsh/js/accessibility.js"></script>
	<script src="decsh/js/main.js"></script>
</body>
</html>
"""
  }
}
