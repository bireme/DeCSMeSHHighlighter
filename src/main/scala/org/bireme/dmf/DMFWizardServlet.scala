/*=========================================================================

    DeCSMeSHFinder © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHFinder/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmf

import jakarta.servlet.{ServletConfig, ServletContext}
import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import java.io.{InputStream, PrintWriter}
import scala.util.{Failure, Success, Try}

/**
  * DeCSMeSHFinder Wizard Servlet
  */
class DMFWizardServlet extends HttpServlet {
  var i18n: I18N = _

  /**
    * Do initial web app configuration
    * @param config servlet config object
    */
  override def init(config: ServletConfig): Unit = {
    super.init(config)

    val context: ServletContext = config.getServletContext
    val i18nIS: InputStream = context.getResourceAsStream("/i18n.txt")

    i18n = new I18N(i18nIS)

    println("DMFWizardServlet is listening ...")
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
    response.setCharacterEncoding("utf-8")

    Try {
      val headerLang: String = getHeaderLang(request)
      val language: String = Option(request.getParameter("lang")).map(_.trim)
        .map(l => if (l.isEmpty) headerLang else l).getOrElse(headerLang)
      val outputText: String = getHtml(language)

      val out: PrintWriter = response.getWriter
      out.println(outputText)
      out.flush()
    } match {
      case Success(_) => ()
      case Failure(ex) => response.sendError(500, ex.getMessage)
    }
  }

  /**
    *
    * @param request HttpServletRequest object
    * @return the desired input/output language according to the request header Accept-Language
    */
  private def getHeaderLang(request: HttpServletRequest): String = {
    val header = Option(request.getHeader("Accept-Language")).map(_.toLowerCase).getOrElse("pt")
    val langs = header.split(",|;")

    langs.find {
      lang => lang.equals("en") || lang.equals("es") || lang.equals("pt") || lang.equals("fr")
    }.getOrElse("pt")
  }

  private def getHtml(language: String): String = {
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
    <meta name="autor" content=" BIREME | OPAS | OMS - > Márcio Alves">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DeCS/MeSH Finder - Wizard</title>
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css">
    <link rel="stylesheet" href="wizardDeCSF/css/bootstrap.min.css">
    <link rel="stylesheet" href="wizardDeCSF/css/wizard.css">
    <link rel="stylesheet" href="wizardDeCSF/css/accessibility.css">
    <link rel="stylesheet" href="wizardDeCSF/css/style.css">
    <link rel="shortcut icon" href="wizardDeCSF/img/favicon.png">
</head>
<body onload="initialClean()">
<script type="text/javascript">

    function initialClean() {
        //alert("entrando no body")
        document.getElementById("text1").value = "";
        document.getElementById("select1").value = "";
        document.getElementById("select2").value = "";
        document.getElementById('main_headings').checked = true;
        document.getElementById('qualifiers').checked = true;
        document.getElementById('entry_terms').checked = true;
        document.getElementById('publication_types').checked = true;
        document.getElementById('check_tags').checked = true;
        document.getElementById('geographics').checked = true;
        document.getElementById('geographics').checked = true;
    }

    function submitPage(plang, useAllTermTypes, leaveWizard) {
//alert("Entrando no submitPage() useAllTermTypes=[" + useAllTermTypes + "]");
        var inputText = document.getElementById("text1").value;
        var inputLang = document.getElementById("select1").value;
        var outputLang = document.getElementById("select2").value;

        var pageLang = """" + language + """";
        var language;
        if (plang === "") language = pageLang;
        else language = plang;

        var form = document.createElement("form");
        form.setAttribute("method", "post");

        if (leaveWizard) form.setAttribute("action", "dmf");
        else form.setAttribute("action", "dmfw");

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
        if (useAllTermTypes) {
            termTypesStr += "Descriptors|Qualifiers";
        } else {
            if (document.getElementById('main_headings').checked) {
              if (termTypesStr !== "") { termTypesStr += "|"; }
              termTypesStr += "Descriptors";
            }
            if (document.getElementById('qualifiers').checked) {
              if (termTypesStr !== "") { termTypesStr += "|"; }
              termTypesStr += "Qualifiers";
            }
        }
//alert("TermTypesStr=" + termTypesStr);
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
        formS.setAttribute("action", "dmfs");

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
               <!--
               <a href="#main_container" tabindex="1" role="button">""" + i18n.translate("Main Content", language) + """ <span class="hiddenMobile">1</span></a>
               <a href="#nav" tabindex="2" role="button">Menu <span class="hiddenMobile">2</span></a>
               <a href="#fieldSearch" tabindex="3" id="accessibilitySearch" role="button">""" + i18n.translate("Search", language) + """ <span class="hiddenMobile">3</span></a>
               <a href="#footer" tabindex="4" role="button">""" + i18n.translate("Footer", language) + """ <span class="hiddenMobile">4</span></a>
                -->
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
				    <div id="language">
                <a href="#" onclick='submitPage("en", false);'>English</a>
                <a href="#" onclick='submitPage("es", false);'>Español</a>
                <a href="#" onclick='submitPage("pt", false);'>Português</a>
                <a href="#" onclick='submitPage("fr", false);'>Français</a>
				    </div>
				    <div class="col-12" id="logoDeCS">
					      <a href="javascript:submitPageToSite('""" + language + """');"><img src="wizardDeCSF/img/logo-green-""" + language + """.svg" alt="" class="imgBlack"></a>
				    </div>
		    </div>
		</div>
</header>
<div class="container">
    <div class="row">
        <div class="col-md-4">
            <h3 class="title">""" + i18n.translate("Choose one of the options below to identify the DeCS/MeSH terms in your text", language) + """.</h3>
            <!--div class="btn-group">
                <a href="#modalWizard" data-toggle="modal" data-target=".bd-example-modal-lg" class="btn btn-lg btn-success">""" + i18n.translate("SIMPLE", language) + """</a>
                <a href="#" class="btn btn-lg btn-outline-success" onclick='submitPage("""" + language + """", true, true)'>""" + i18n.translate("ADVANCED", language) + """</a>
            </div-->
            <div class="btn-group">
              <!-- Botão SIMPLE -->
              <a href="#modalWizard" data-toggle="modal" data-target=".bd-example-modal-lg" class="btn btn-lg btn-success" id="simpleButton">""" + i18n.translate("SIMPLE", language) + """</a>
              <!-- Botão ADVANCED -->
              <a href="#" class="btn btn-lg btn-outline-success" id="advancedButton" onclick='submitPage("""" + language + """", true, true)'>""" + i18n.translate("ADVANCED", language) + """</a>
            </div>
        </div>
        <div class="col-md-8">
            <img src="wizardDeCSF/img/telas.png" class="img-fluid" alt="">
        </div>
    </div>
</div>
<footer id="footer" class="padding1">
    <div class="container">
        <div class="row">
            <div class="col-md-5">
                <b>DeCS/MeSH Finder</b> <br>
                <a href="http://politicas.bireme.org/terminos/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Terms and conditions of use", language) + """</a>
					      <a href="http://politicas.bireme.org/privacidad/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Privacy policy", language) + """</a>
            </div>
            <div class="col-md-7 text-right">
                <img src="http://logos.bireme.org/img/""" + language + """/h_bir_white.svg" alt="" class="img-fluid">
            </div>
        </div>
    </div>
</footer>
<div class="modal fade bd-example-modal-lg " tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header wizardModalTitle">
                <h4 class="text-center" >""" + i18n.translate("Choose one of the options below to identify the DeCS/MeSH terms in your text", language) + """. <br> <small>""" + i18n.translate("Answer the following questions", language) + """:</small></h4>
            </div>
            <!-- Etapa1 -->
            <div class="modal-body wizardModalBody" id="etapa1">
                <div>
                    <ul id="wizard">
                        <li><i class="fas fa-circle icone3"></i><i class="fas fa-chevron-right icone"></i></li>
                        <li><i class="fas fa-circle icone2"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("What is the language of your text", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <select name="" id="select1" class="form-control wizardColor">
                            <option value="" disabled="" selected data-toggle="collapse" data-target="#etapa2">""" + i18n.translate("Select", language) + """</option>
                            <option value="All Languages">""" + i18n.translate("I don't know", language) + """</option>
                            <option value="en">""" + i18n.translate("English", language) + """</option>
                            <option value="es">""" + i18n.translate("Spanish", language) + """</option>
                            <option value="pt">""" + i18n.translate("Portuguese", language) + """</option>
                            <option value="fr">""" + i18n.translate("French", language) + """</option>
                        </select>
                    </div>
                </div>
            </div>
            <!-- Etapa 2 -->
            <div class="modal-body wizardModalBody collapse" id="etapa2">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone3"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("Paste your text below", language) + """</h5>
                    </div>
                    <div class="col-12 marginTB1">
                        <textarea name="" id="text1" cols="30" rows="10" class="form-control"></textarea>
                    </div>
                    <div class="col-12 marginTB1">
                        <button type="button" class="btn btn-success" id="btEtapa2">""" + i18n.translate("Next", language) + """</button>
                    </div>
                </div>
            </div>
            <!-- Etapa3 -->
            <div class="modal-body wizardModalBody collapse" id="etapa3">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc2">""" + i18n.translate("Paste your text below", language) + """</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone3"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("In what language do you want to see the DeCS/MeSH terms", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <select name="" id="select2" class="form-control wizardColor2">
                            <option value="" disabled="" selected>""" + i18n.translate("Select", language) + """</option>
                            <option value="All Languages">""" + i18n.translate("The same found in the text", language) + """</option>
                            <option value="en">""" + i18n.translate("English", language) + """</option>
                            <option value="es">""" + i18n.translate("Spanish", language) + """</option>
                            <option value="pt">""" + i18n.translate("Portuguese", language) + """</option>
                            <option value="fr">""" + i18n.translate("French", language) + """</option>
                        </select>
                    </div>
                </div>
            </div>
            <!-- etapa 4 -->
            <div class="modal-body wizardModalBody collapse" id="etapa4">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc2">""" + i18n.translate("Paste your text below", language) + """</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc3">""" + i18n.translate("In what language do you want to see the DeCS/MeSH terms", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone3"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("Do you want to use advanced filters", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <div class="row marginTB1">
                            <div class="col-md-6 marginTB1">
                                <div class="card h-100 cartbt wizardColor3" id="btEtapa4b">
                                    <a href="#" style="text-decoration: none;" onclick='submitPage("""" + language + """", true, true)'>
                                        <div class="card-body">
                                          """ + i18n.translate("No, proceed with the identification of DeCS/MeSH terms", language) + """
                                        </div>
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="card h-100 cartbt wizardColor3" id="btEtapa4">
                                    <div class="card-body">
                                      """ + i18n.translate("Yes, go to them", language) + """
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- etapa 5 -->
            <div class="modal-body wizardModalBody collapse" id="etapa5">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc2">""" + i18n.translate("Paste your text below", language) + """</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc3">""" + i18n.translate("In what language do you want to see the DeCS/MeSH terms", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc4">""" + i18n.translate("Do you want to use advanced filters", language) + """?</a></li>
                        <!--li><a href="#!" class="bc4">""" + i18n.translate("Which types of terms do you want to identify", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li-->
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("Which types of terms do you want to identify", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <div class="row marginTB1">
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="main_headings">
                                    <label class="custom-control-label" for="main_headings">""" + i18n.translate("Descriptors", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="qualifiers">
                                    <label class="custom-control-label" for="qualifiers">""" + i18n.translate("Qualifiers", language) + """</label>
                                </div>
                            </div>
                            <!--div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="entry_terms">
                                    <label class="custom-control-label" for="entry_terms">""" + i18n.translate("Entry terms", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="publication_types">
                                    <label class="custom-control-label" for="publication_types">""" + i18n.translate("Publication types", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="check_tags">
                                    <label class="custom-control-label" for="check_tags">""" + i18n.translate("Check tags", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="geographics">
                                    <label class="custom-control-label" for="geographics">""" + i18n.translate("Geographics", language) + """</label>
                                </div>
                            </div-->
                        </div>
                        <div class="modal-footer">
                            <a href="#" class="btn btn-success" onclick='submitPage("""" + language + """", false, true)'>""" + i18n.translate("Search", language) + """</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
  // Captura o clique no botão SIMPLE
  document.getElementById('simpleButton').addEventListener('click', function() {
      // Verifica se o Google Analytics está disponível
      if (typeof gtag === 'function') {
          // Envia o evento para o Google Analytics
          gtag('event', 'button_click', {
              'event_category': 'interaction',
              'event_label': 'SIMPLE', // Nome do botão
              'value': 'simple_button_click' // Pode ser qualquer valor ou nome descritivo
          });
      } else {
          console.error('Google Analytics não está disponível.');
      }
  });

  // Captura o clique no botão ADVANCED
  document.getElementById('advancedButton').addEventListener('click', function() {
      // Verifica se o Google Analytics está disponível
      if (typeof gtag === 'function') {
          // Envia o evento para o Google Analytics
          gtag('event', 'button_click', {
              'event_category': 'interaction',
              'event_label': 'ADVANCED', // Nome do botão
              'value': 'advanced_button_click' // Pode ser qualquer valor ou nome descritivo
          });
      } else {
          console.error('Google Analytics não está disponível.');
      }
  });
</script>

<script src="wizardDeCSF/js/jquery-3.3.1.min.js"></script>
<script src="wizardDeCSF/js/bootstrap.min.js"></script>
<script src="wizardDeCSF/js/cookie.js"></script>
<script src="wizardDeCSF/js/accessibility.js"></script>
<script src="wizardDeCSF/js/wizard.js"></script>
</body>
</html>
    """
  }
}
